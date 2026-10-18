package cl.duoc.agenciaoportunidades.ui.detalle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.util.estadosPermitidos
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.puedeAsignarResponsable
import cl.duoc.agenciaoportunidades.util.puedeVerOportunidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleOportunidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var oportunidadId: Int? = null

    fun cargar(id: Int, usuarioActual: Usuario) {
        if (oportunidadId == id) return
        oportunidadId = id
        usuario = usuarioActual

        viewModelScope.launch {
            repository.observarDetalleOportunidad(id).collect { detalle ->
                // Un ejecutivo no puede abrir oportunidades asignadas a otra persona
                val sinAcceso = detalle != null &&
                        !puedeVerOportunidad(usuarioActual, detalle.responsableId)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        detalle = if (sinAcceso) null else detalle,
                        sinAcceso = sinAcceso
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.obtenerSeguimientos(id).collect { lista ->
                _uiState.update { it.copy(seguimientos = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerHistorialEstados(id).collect { lista ->
                _uiState.update { it.copy(historialEstados = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerEjecutivos().collect { lista ->
                _uiState.update { it.copy(ejecutivos = lista) }
            }
        }
    }

    fun cambiarEstado(nuevoEstado: EstadoOportunidad, observacion: String) {
        val usuarioActual = usuario ?: return
        val id = oportunidadId ?: return

        viewModelScope.launch {
            try {
                val oportunidad = repository.obtenerOportunidad(id)
                if (oportunidad == null) {
                    mostrarMensaje("No se encontró la oportunidad")
                    return@launch
                }
                if (nuevoEstado !in estadosPermitidos(usuarioActual, oportunidad.estado)) {
                    mostrarMensaje("No tienes permiso para realizar este cambio de estado")
                    return@launch
                }
                repository.cambiarEstado(
                    oportunidad = oportunidad,
                    nuevoEstado = nuevoEstado,
                    usuarioId = usuarioActual.id,
                    observaciones = observacion.trim()
                )
                mostrarMensaje("Estado actualizado a \"${nuevoEstado.etiqueta()}\"")
            } catch (e: Exception) {
                mostrarMensaje("No se pudo actualizar el estado. Inténtalo nuevamente")
            }
        }
    }

    // responsableId null = dejar la oportunidad sin asignar
    fun asignarResponsable(responsableId: Int?) {
        val usuarioActual = usuario ?: return
        val id = oportunidadId ?: return

        if (!puedeAsignarResponsable(usuarioActual)) {
            mostrarMensaje("Solo el supervisor puede asignar responsables")
            return
        }

        viewModelScope.launch {
            try {
                val oportunidad = repository.obtenerOportunidad(id)
                if (oportunidad == null) {
                    mostrarMensaje("No se encontró la oportunidad")
                    return@launch
                }
                repository.asignarResponsable(oportunidad, responsableId)
                mostrarMensaje(
                    if (responsableId == null) "La oportunidad quedó sin asignar"
                    else "Responsable asignado correctamente"
                )
            } catch (e: Exception) {
                mostrarMensaje("No se pudo asignar el responsable. Inténtalo nuevamente")
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun mostrarMensaje(texto: String) {
        _uiState.update { it.copy(mensaje = texto) }
    }
}