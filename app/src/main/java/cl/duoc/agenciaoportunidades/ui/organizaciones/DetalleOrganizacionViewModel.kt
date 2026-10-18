package cl.duoc.agenciaoportunidades.ui.organizaciones

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleOrganizacionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(DetalleOrganizacionUiState())
    val uiState: StateFlow<DetalleOrganizacionUiState> = _uiState.asStateFlow()

    private var cargado: Int? = null

    fun cargar(id: Int) {
        if (cargado == id) return
        cargado = id

        // Organización y su comuna (se actualiza sola al editar)
        viewModelScope.launch {
            combine(
                repository.observarOrganizacion(id),
                repository.obtenerComunas()
            ) { organizacion, comunas ->
                Pair(organizacion, comunas.find { it.id == organizacion?.comunaId })
            }.collect { (organizacion, comuna) ->
                _uiState.update {
                    it.copy(cargando = false, organizacion = organizacion, comuna = comuna)
                }
            }
        }
        viewModelScope.launch {
            repository.obtenerContactosPorOrganizacion(id).collect { lista ->
                _uiState.update { it.copy(contactos = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerEventosPorOrganizacion(id).collect { lista ->
                _uiState.update { it.copy(eventos = lista) }
            }
        }
    }
}