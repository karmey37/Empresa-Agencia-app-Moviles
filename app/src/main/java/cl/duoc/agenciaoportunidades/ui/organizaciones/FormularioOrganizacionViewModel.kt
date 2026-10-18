package cl.duoc.agenciaoportunidades.ui.organizaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.Organizacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioOrganizacionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(FormularioOrganizacionUiState())
    val uiState: StateFlow<FormularioOrganizacionUiState> = _uiState.asStateFlow()

    private var original: Organizacion? = null
    private var existentes: List<Organizacion> = emptyList()
    private var iniciado = false

    fun iniciar(organizacionId: Int?) {
        if (iniciado) return
        iniciado = true

        viewModelScope.launch {
            repository.obtenerComunas().collect { lista ->
                _uiState.update { it.copy(comunas = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerOrganizaciones().collect { lista ->
                existentes = lista
            }
        }
        viewModelScope.launch {
            if (organizacionId == null) {
                _uiState.update { it.copy(cargando = false) }
                return@launch
            }
            val organizacion = repository.obtenerOrganizacion(organizacionId)
            if (organizacion == null) {
                _uiState.update { it.copy(cargando = false, noEncontrada = true) }
                return@launch
            }
            original = organizacion
            _uiState.update {
                it.copy(
                    cargando = false,
                    esEdicion = true,
                    nombre = organizacion.nombre,
                    tipo = organizacion.tipo,
                    rubro = organizacion.rubro,
                    comunaId = organizacion.comunaId,
                    sitioWeb = organizacion.sitioWeb,
                    publicoAsociado = organizacion.publicoAsociado,
                    observaciones = organizacion.observaciones
                )
            }
        }
    }

    fun actualizar(
        campo: CampoOrganizacion? = null,
        cambio: (FormularioOrganizacionUiState) -> FormularioOrganizacionUiState
    ) {
        _uiState.update { actual ->
            val nuevo = cambio(actual)
            if (campo == null) nuevo else nuevo.copy(errores = nuevo.errores - campo)
        }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        val errores = validarFormularioOrganizacion(estado).toMutableMap()

        // Evita duplicar organizaciones: el nombre debe ser único
        val previa = original
        val nombreRepetido = existentes.any {
            it.id != (previa?.id ?: -1) &&
                    it.nombre.trim().equals(estado.nombre.trim(), ignoreCase = true)
        }
        if (nombreRepetido && !errores.containsKey(CampoOrganizacion.NOMBRE)) {
            errores[CampoOrganizacion.NOMBRE] = "Ya existe una organización con ese nombre"
        }

        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update { it.copy(errores = emptyMap(), guardando = true) }

        viewModelScope.launch {
            try {
                val comunaId = estado.comunaId
                    ?: throw IllegalStateException("Falta la comuna")

                val idGuardado: Int
                if (previa != null) {
                    repository.actualizarOrganizacion(
                        previa.copy(
                            nombre = estado.nombre.trim(),
                            tipo = estado.tipo,
                            rubro = estado.rubro.trim(),
                            comunaId = comunaId,
                            sitioWeb = estado.sitioWeb.trim(),
                            publicoAsociado = estado.publicoAsociado.trim(),
                            observaciones = estado.observaciones.trim()
                        )
                    )
                    idGuardado = previa.id
                } else {
                    idGuardado = repository.guardarOrganizacion(
                        Organizacion(
                            nombre = estado.nombre.trim(),
                            tipo = estado.tipo,
                            rubro = estado.rubro.trim(),
                            comunaId = comunaId,
                            sitioWeb = estado.sitioWeb.trim(),
                            publicoAsociado = estado.publicoAsociado.trim(),
                            observaciones = estado.observaciones.trim()
                        )
                    )
                }
                _uiState.update { it.copy(guardando = false, guardadoId = idGuardado) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No se pudo guardar la organización. Inténtalo nuevamente"
                    )
                }
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}