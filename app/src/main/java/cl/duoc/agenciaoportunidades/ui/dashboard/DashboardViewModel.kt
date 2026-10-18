package cl.duoc.agenciaoportunidades.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.util.estaAbierta
import cl.duoc.agenciaoportunidades.util.eventosProximos
import cl.duoc.agenciaoportunidades.util.puedeVerOportunidad
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var trabajo: Job? = null
    private var usuarioId: Int? = null

    fun cargar(usuario: Usuario) {
        if (usuarioId == usuario.id) return
        usuarioId = usuario.id

        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            combine(
                repository.obtenerResumenes(usuario),
                repository.obtenerSeguimientosAtrasados(),
                repository.obtenerSeguimientosPendientes(),
                repository.obtenerSinSeguimiento()
            ) { resumenes, atrasados, pendientes, sinSeguimiento ->
                val ahora = System.currentTimeMillis()
                DashboardUiState(
                    cargando = false,
                    ahora = ahora,
                    resumenes = resumenes,
                    eventosProximos = eventosProximos(resumenes, ahora),
                    seguimientosAtrasados = atrasados.filter {
                        puedeVerOportunidad(usuario, it.responsableId)
                    },
                    seguimientosPendientes = pendientes.filter {
                        puedeVerOportunidad(usuario, it.responsableId)
                    },
                    sinSeguimiento = sinSeguimiento.filter {
                        puedeVerOportunidad(usuario, it.responsableId) && estaAbierta(it.estado)
                    }
                )
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }
}