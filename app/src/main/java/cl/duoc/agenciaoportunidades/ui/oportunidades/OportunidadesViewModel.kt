package cl.duoc.agenciaoportunidades.ui.oportunidades

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OportunidadesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(OportunidadesUiState())
    val uiState: StateFlow<OportunidadesUiState> = _uiState.asStateFlow()

    private var trabajo: Job? = null

    //e repositorio decide que oportunidades ve el usuario segun su perfil
    fun cargar(usuario: Usuario) {
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            repository.obtenerResumenes(usuario).collect { lista ->
                _uiState.update { it.copy(cargando = false, oportunidades = lista) }
            }
        }
    }

    fun cambiarBusqueda(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }

    fun cambiarEstadoFiltro(estado: EstadoOportunidad?) {
        _uiState.update { it.copy(estadoFiltro = estado) }
    }

    fun cambiarPrioridadFiltro(prioridad: Prioridad?) {
        _uiState.update { it.copy(prioridadFiltro = prioridad) }
    }
}