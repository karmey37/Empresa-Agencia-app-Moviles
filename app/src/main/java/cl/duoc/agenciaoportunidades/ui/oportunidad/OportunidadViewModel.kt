package cl.duoc.agenciaoportunidades.ui.oportunidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.model.Oportunidad
// Asegúrate de importar tu repositorio real si ya lo tienes creado
// import cl.duoc.agenciaoportunidades.data.OportunidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OportunidadUiState(
    val listaOportunidades: List<Oportunidad> = emptyList(),
    val cargando: Boolean = false,
    val filtroEstado: String = "Todos"
)

// Nota: Reemplaza "Any" por tu Repositorio real cuando lo conectes a la BD
class OportunidadViewModel(/* private val repository: OportunidadRepository */) : ViewModel() {

    private val _uiState = MutableStateFlow(OportunidadUiState())
    val uiState: StateFlow<OportunidadUiState> = _uiState.asStateFlow()

    fun cargarOportunidades() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }

            // Simulación de carga. Aquí llamarás a repository.obtenerTodas()
            val datos = emptyList<Oportunidad>()

            _uiState.update { it.copy(listaOportunidades = datos, cargando = false) }
        }
    }
}