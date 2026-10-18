package cl.duoc.agenciaoportunidades.ui.contactos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.Contacto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioContactoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(FormularioContactoUiState())
    val uiState: StateFlow<FormularioContactoUiState> = _uiState.asStateFlow()

    private var organizacionId: Int? = null
    private var original: Contacto? = null
    private var existentes: List<Contacto> = emptyList()
    private var iniciado = false

    fun iniciar(idOrganizacion: Int, contactoId: Int?) {
        if (iniciado) return
        iniciado = true
        organizacionId = idOrganizacion

        viewModelScope.launch {
            repository.obtenerContactosPorOrganizacion(idOrganizacion).collect { lista ->
                existentes = lista
            }
        }

        viewModelScope.launch {
            val organizacion = repository.obtenerOrganizacion(idOrganizacion)
            if (organizacion == null) {
                _uiState.update { it.copy(cargando = false, noEncontrado = true) }
                return@launch
            }
            _uiState.update { it.copy(organizacionNombre = organizacion.nombre) }

            if (contactoId == null) {
                _uiState.update { it.copy(cargando = false) }
                return@launch
            }

            val contacto = repository.obtenerContacto(contactoId)
            if (contacto == null || contacto.organizacionId != idOrganizacion) {
                _uiState.update { it.copy(cargando = false, noEncontrado = true) }
                return@launch
            }

            original = contacto
            _uiState.update {
                it.copy(
                    cargando = false,
                    esEdicion = true,
                    nombre = contacto.nombre,
                    cargo = contacto.cargo,
                    area = contacto.area,
                    correo = contacto.correo,
                    telefono = contacto.telefono,
                    medio = contacto.medioPreferente,
                    estadoContacto = contacto.estado,
                    observaciones = contacto.observaciones
                )
            }
        }
    }

    fun actualizar(
        campo: CampoContacto? = null,
        cambio: (FormularioContactoUiState) -> FormularioContactoUiState
    ) {
        _uiState.update { actual ->
            val nuevo = cambio(actual)
            if (campo == null) nuevo else nuevo.copy(errores = nuevo.errores - campo)
        }
    }

    fun guardar() {
        val idOrganizacion = organizacionId ?: return
        val estado = _uiState.value
        if (estado.guardando) return

        val errores = validarFormularioContacto(estado).toMutableMap()

        // Evita duplicar contactos: el correo debe ser único dentro de la organización
        val previo = original
        val correoRepetido = existentes.any {
            it.id != (previo?.id ?: -1) &&
                    it.correo.trim().equals(estado.correo.trim(), ignoreCase = true)
        }
        if (correoRepetido && !errores.containsKey(CampoContacto.CORREO)) {
            errores[CampoContacto.CORREO] = "Ya existe un contacto con ese correo en esta organización"
        }

        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update { it.copy(errores = emptyMap(), guardando = true) }

        viewModelScope.launch {
            try {
                val idGuardado: Int
                if (previo != null) {
                    repository.actualizarContacto(
                        previo.copy(
                            nombre = estado.nombre.trim(),
                            cargo = estado.cargo.trim(),
                            area = estado.area,
                            correo = estado.correo.trim().lowercase(),
                            telefono = estado.telefono.replace(" ", ""),
                            medioPreferente = estado.medio,
                            estado = estado.estadoContacto,
                            observaciones = estado.observaciones.trim()
                        )
                    )
                    idGuardado = previo.id
                } else {
                    idGuardado = repository.guardarContacto(
                        Contacto(
                            organizacionId = idOrganizacion,
                            nombre = estado.nombre.trim(),
                            cargo = estado.cargo.trim(),
                            area = estado.area,
                            correo = estado.correo.trim().lowercase(),
                            telefono = estado.telefono.replace(" ", ""),
                            medioPreferente = estado.medio,
                            estado = estado.estadoContacto,
                            fechaUltimoContacto = null,
                            observaciones = estado.observaciones.trim()
                        )
                    )
                }
                _uiState.update { it.copy(guardando = false, guardadoId = idGuardado) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No se pudo guardar el contacto. Inténtalo nuevamente"
                    )
                }
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}