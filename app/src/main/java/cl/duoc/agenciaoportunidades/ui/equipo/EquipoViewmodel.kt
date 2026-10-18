package cl.duoc.agenciaoportunidades.ui.equipo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.util.esSupervisor
import cl.duoc.agenciaoportunidades.util.estaAbierta
import cl.duoc.agenciaoportunidades.util.puedeAsignarResponsable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(EquipoUiState())
    val uiState: StateFlow<EquipoUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var iniciado = false

    fun cargar(usuarioActual: Usuario) {
        if (iniciado) return
        iniciado = true
        usuario = usuarioActual

        // Este panel es solo para el supervisor
        if (!esSupervisor(usuarioActual)) {
            _uiState.update { it.copy(cargando = false, sinAcceso = true) }
            return
        }

        viewModelScope.launch {
            combine(
                repository.obtenerResumenes(usuarioActual),
                repository.obtenerEjecutivos(),
                repository.obtenerSeguimientosAtrasados()
            ) { resumenes, ejecutivos, atrasados ->
                val porEjecutivo = ejecutivos.map { ejecutivo ->
                    val suyas = resumenes.filter { it.responsableId == ejecutivo.id }
                    ResumenEjecutivo(
                        ejecutivo = ejecutivo,
                        asignadas = suyas.size,
                        abiertas = suyas.count { estaAbierta(it.estado) },
                        altaPrioridad = suyas.count {
                            estaAbierta(it.estado) && it.prioridad == Prioridad.ALTA
                        },
                        atrasadas = atrasados.count { it.responsableId == ejecutivo.id }
                    )
                }
                EquipoUiState(
                    cargando = false,
                    ejecutivos = porEjecutivo,
                    sinAsignar = resumenes.filter {
                        it.responsableId == null && estaAbierta(it.estado)
                    },
                    totalOportunidades = resumenes.size,
                    totalAtrasadas = atrasados.size
                )
            }.collect { nuevo ->
                _uiState.update { actual -> nuevo.copy(mensaje = actual.mensaje) }
            }
        }
    }

    fun asignar(oportunidadId: Int, responsableId: Int) {
        val usuarioActual = usuario ?: return
        if (!puedeAsignarResponsable(usuarioActual)) {
            mostrarMensaje("Solo el supervisor puede asignar responsables")
            return
        }

        viewModelScope.launch {
            try {
                val oportunidad = repository.obtenerOportunidad(oportunidadId)
                if (oportunidad == null) {
                    mostrarMensaje("No se encontró la oportunidad")
                    return@launch
                }
                repository.asignarResponsable(oportunidad, responsableId)
                mostrarMensaje("Responsable asignado correctamente")
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