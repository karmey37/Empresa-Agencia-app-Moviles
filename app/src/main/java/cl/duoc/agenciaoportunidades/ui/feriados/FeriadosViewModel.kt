package cl.duoc.agenciaoportunidades.ui.feriados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.externa.FeriadosRepository
import cl.duoc.agenciaoportunidades.data.externa.mensajeDeErrorFeriados
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeriadosViewModel(
    private val repository: FeriadosRepository = FeriadosRepository(),
    hoy: String = fechaHoyIso()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FeriadosUiState(anio = hoy.take(4).toInt(), hoy = hoy)
    )
    val uiState: StateFlow<FeriadosUiState> = _uiState.asStateFlow()

    // Para cancelar la consulta anterior si el usuario cambia de año muy rápido
    private var consulta: Job? = null

    init {
        cargar()
    }

    // GET /api/v3/PublicHolidays/{anio}/CL
    fun cargar() {
        val anio = _uiState.value.anio
        _uiState.update { it.copy(cargando = true) }
        consulta?.cancel()
        consulta = viewModelScope.launch {
            repository.obtener(anio)
                .onSuccess { lista ->
                    _uiState.update { it.copy(cargando = false, feriados = lista) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            feriados = emptyList(),
                            mensaje = mensajeDeErrorFeriados(error)
                        )
                    }
                }
        }
    }

    // delta = -1 (año anterior) o +1 (año siguiente)
    fun cambiarAnio(delta: Int) {
        _uiState.update { it.copy(anio = it.anio + delta) }
        cargar()
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
