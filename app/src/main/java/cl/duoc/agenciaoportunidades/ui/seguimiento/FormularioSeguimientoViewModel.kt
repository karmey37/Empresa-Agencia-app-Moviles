package cl.duoc.agenciaoportunidades.ui.seguimiento

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.Seguimiento
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.util.diasHasta
import cl.duoc.agenciaoportunidades.util.formatearFecha
import cl.duoc.agenciaoportunidades.util.parsearFecha
import cl.duoc.agenciaoportunidades.util.puedeEditarOportunidad
import cl.duoc.agenciaoportunidades.util.puedeVerOportunidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioSeguimientoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    // La fecha de la gestión parte con la fecha de hoy
    private val _uiState = MutableStateFlow(
        FormularioSeguimientoUiState(fecha = formatearFecha(System.currentTimeMillis()))
    )
    val uiState: StateFlow<FormularioSeguimientoUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var oportunidadId: Int? = null
    private var iniciado = false

    fun iniciar(usuarioActual: Usuario, id: Int) {
        if (iniciado) return
        iniciado = true
        usuario = usuarioActual
        oportunidadId = id

        viewModelScope.launch {
            val oportunidad = repository.obtenerOportunidad(id)
            val evento = oportunidad?.let { repository.obtenerEvento(it.eventoId) }

            if (oportunidad == null || evento == null) {
                _uiState.update { it.copy(cargando = false, noEncontrada = true) }
                return@launch
            }
            // Solo el responsable (o el supervisor) puede registrar gestiones, y mientras esté abierta
            if (!puedeVerOportunidad(usuarioActual, oportunidad.responsableId) ||
                !puedeEditarOportunidad(usuarioActual, oportunidad.estado)
            ) {
                _uiState.update { it.copy(cargando = false, sinPermiso = true) }
                return@launch
            }

            _uiState.update { it.copy(cargando = false, oportunidadNombre = oportunidad.nombre) }

            // Contactos de la organización del evento
            repository.obtenerContactosPorOrganizacion(evento.organizacionId).collect { lista ->
                _uiState.update { it.copy(contactos = lista) }
            }
        }
    }

    fun actualizar(
        campo: CampoSeguimiento? = null,
        cambio: (FormularioSeguimientoUiState) -> FormularioSeguimientoUiState
    ) {
        _uiState.update { actual ->
            val nuevo = cambio(actual)
            if (campo == null) nuevo else nuevo.copy(errores = nuevo.errores - campo)
        }
    }

    fun guardar() {
        val usuarioActual = usuario ?: return
        val id = oportunidadId ?: return
        val estado = _uiState.value
        if (estado.guardando) return

        val ahora = System.currentTimeMillis()
        val errores = validarFormularioSeguimiento(estado, ahora)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update { it.copy(errores = emptyMap(), guardando = true) }

        viewModelScope.launch {
            try {
                val fechaBase = parsearFecha(estado.fecha)
                    ?: throw IllegalStateException("Fecha inválida")
                // Si la gestión es de hoy se guarda con la hora actual; si no, a mediodía
                val fechaHora = if (diasHasta(fechaBase, ahora) == 0) {
                    ahora
                } else {
                    fechaBase + 12L * 60 * 60 * 1000
                }

                val seguimientoId = repository.registrarSeguimiento(
                    Seguimiento(
                        oportunidadId = id,
                        usuarioId = usuarioActual.id,
                        contactoId = estado.contactoId,
                        tipoAccion = estado.tipoAccion,
                        fechaHora = fechaHora,
                        resultado = estado.resultado.trim(),
                        proximaAccion = estado.proximaAccion.trim(),
                        fechaProximoSeguimiento = if (estado.fechaProxima.isBlank()) {
                            null
                        } else {
                            parsearFecha(estado.fechaProxima)
                        },
                        observaciones = estado.observaciones.trim()
                    )
                )
                _uiState.update { it.copy(guardando = false, guardadoId = seguimientoId) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No se pudo registrar el seguimiento. Inténtalo nuevamente"
                    )
                }
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}