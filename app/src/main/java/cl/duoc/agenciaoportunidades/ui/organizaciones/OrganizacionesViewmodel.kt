package cl.duoc.agenciaoportunidades.ui.organizaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrganizacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(OrganizacionesUiState())
    val uiState: StateFlow<OrganizacionesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.obtenerResumenesOrganizaciones().collect { lista ->
                _uiState.update { it.copy(cargando = false, organizaciones = lista) }
            }
        }
    }

    fun cambiarBusqueda(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }
}