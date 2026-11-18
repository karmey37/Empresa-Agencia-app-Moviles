package cl.duoc.agenciaoportunidades.ui.directorio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.remote.OrganizacionDto
import cl.duoc.agenciaoportunidades.data.remote.OrganizacionesRemoteRepository
import cl.duoc.agenciaoportunidades.data.remote.mensajeDeError
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DirectorioViewModel(
    private val repository: OrganizacionesRemoteRepository = OrganizacionesRemoteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DirectorioUiState())
    val uiState: StateFlow<DirectorioUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    // GET /organizaciones
    fun cargar() {
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            repository.listar()
                .onSuccess { lista ->
                    _uiState.update { actual ->
                        actual.copy(
                            cargando = false,
                            organizaciones = lista.sortedBy { org -> org.nombre.lowercase() }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { actual ->
                        actual.copy(cargando = false, mensaje = mensajeDeError(error))
                    }
                }
        }
    }

    fun cambiarBusqueda(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    // ---------- formulario ----------
    fun abrirNuevo() {
        _uiState.update {
            it.copy(
                formularioAbierto = true,
                editandoId = null,
                nombre = "",
                tipo = TipoOrganizacion.EMPRESA,
                rubro = "",
                comuna = "",
                sitioWeb = "",
                publicoAsociado = "",
                observaciones = "",
                errores = emptyMap()
            )
        }
    }

    fun abrirEditar(org: OrganizacionDto) {
        _uiState.update {
            it.copy(
                formularioAbierto = true,
                editandoId = org.id,
                nombre = org.nombre,
                tipo = runCatching { TipoOrganizacion.valueOf(org.tipo) }
                    .getOrDefault(TipoOrganizacion.OTRA),
                rubro = org.rubro,
                comuna = org.comuna,
                sitioWeb = org.sitioWeb.orEmpty(),
                publicoAsociado = org.publicoAsociado.orEmpty(),
                observaciones = org.observaciones.orEmpty(),
                errores = emptyMap()
            )
        }
    }

    fun cerrarFormulario() {
        _uiState.update { it.copy(formularioAbierto = false, errores = emptyMap()) }
    }

    fun actualizar(
        campo: CampoDirectorio? = null,
        cambio: (DirectorioUiState) -> DirectorioUiState
    ) {
        _uiState.update { actual ->
            val nuevo = cambio(actual)
            if (campo == null) nuevo else nuevo.copy(errores = nuevo.errores - campo)
        }
    }

    // POST /organizaciones (crear) o PUT /organizaciones/{id} (editar)
    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        val errores = validarDirectorio(estado)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update { it.copy(errores = emptyMap(), guardando = true) }

        val dto = OrganizacionDto(
            nombre = estado.nombre.trim(),
            tipo = estado.tipo.name,
            rubro = estado.rubro.trim(),
            comuna = estado.comuna.trim(),
            sitioWeb = estado.sitioWeb.trim(),
            publicoAsociado = estado.publicoAsociado.trim(),
            observaciones = estado.observaciones.trim()
        )

        viewModelScope.launch {
            val id = estado.editandoId
            val resultado = if (id == null) {
                repository.crear(dto)
            } else {
                repository.actualizar(id, dto)
            }

            resultado
                .onSuccess {
                    _uiState.update { actual ->
                        actual.copy(
                            guardando = false,
                            formularioAbierto = false,
                            mensaje = if (id == null) {
                                "Organización creada en el servidor"
                            } else {
                                "Organización actualizada en el servidor"
                            }
                        )
                    }
                    cargar()
                }
                .onFailure { error ->
                    _uiState.update { actual ->
                        actual.copy(guardando = false, mensaje = mensajeDeError(error))
                    }
                }
        }
    }

    // ---------- borrar ----------
    fun pedirEliminar(org: OrganizacionDto) {
        _uiState.update { it.copy(aEliminar = org) }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(aEliminar = null) }
    }

    // DELETE /organizaciones/{id}
    fun confirmarEliminar() {
        val org = _uiState.value.aEliminar ?: return
        val id = org.id ?: return
        _uiState.update { it.copy(aEliminar = null) }

        viewModelScope.launch {
            repository.borrar(id)
                .onSuccess {
                    _uiState.update { actual ->
                        actual.copy(mensaje = "Organización eliminada del servidor")
                    }
                    cargar()
                }
                .onFailure { error ->
                    _uiState.update { actual ->
                        actual.copy(mensaje = mensajeDeError(error))
                    }
                }
        }
    }
}