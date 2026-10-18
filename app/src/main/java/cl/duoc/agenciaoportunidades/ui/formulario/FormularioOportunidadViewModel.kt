package cl.duoc.agenciaoportunidades.ui.formulario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.Evento
import cl.duoc.agenciaoportunidades.model.Oportunidad
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.util.esSupervisor
import cl.duoc.agenciaoportunidades.util.formatearFecha
import cl.duoc.agenciaoportunidades.util.parsearFecha
import cl.duoc.agenciaoportunidades.util.puedeEditarOportunidad
import cl.duoc.agenciaoportunidades.util.puedeVerOportunidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioOportunidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(FormularioOportunidadUiState())
    val uiState: StateFlow<FormularioOportunidadUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var oportunidadOriginal: Oportunidad? = null
    private var eventoOriginal: Evento? = null
    private var iniciado = false

    fun iniciar(usuarioActual: Usuario, oportunidadId: Int?) {
        if (iniciado) return
        iniciado = true
        usuario = usuarioActual

        // Listas para los campos desplegables
        viewModelScope.launch {
            repository.obtenerOrganizaciones().collect { lista ->
                _uiState.update { it.copy(organizaciones = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerComunas().collect { lista ->
                _uiState.update { it.copy(comunas = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerEjecutivos().collect { lista ->
                _uiState.update { it.copy(ejecutivos = lista) }
            }
        }

        viewModelScope.launch {
            if (oportunidadId == null) {
                // Oportunidad nueva: el ejecutivo queda como responsable
                _uiState.update {
                    it.copy(
                        cargando = false,
                        responsableId = if (esSupervisor(usuarioActual)) null else usuarioActual.id
                    )
                }
            } else {
                cargarParaEditar(oportunidadId, usuarioActual)
            }
        }
    }

    private suspend fun cargarParaEditar(id: Int, usuarioActual: Usuario) {
        val oportunidad = repository.obtenerOportunidad(id)
        val evento = oportunidad?.let { repository.obtenerEvento(it.eventoId) }

        if (oportunidad == null || evento == null) {
            _uiState.update { it.copy(cargando = false, noEncontrada = true) }
            return
        }
        if (!puedeVerOportunidad(usuarioActual, oportunidad.responsableId) ||
            !puedeEditarOportunidad(usuarioActual, oportunidad.estado)
        ) {
            _uiState.update { it.copy(cargando = false, sinPermiso = true) }
            return
        }

        oportunidadOriginal = oportunidad
        eventoOriginal = evento

        _uiState.update {
            it.copy(
                cargando = false,
                esEdicion = true,
                nombre = oportunidad.nombre,
                descripcion = oportunidad.descripcion,
                fuente = oportunidad.fuente,
                responsableId = oportunidad.responsableId,
                prioridad = oportunidad.prioridad,
                potencial = oportunidad.potencialComercial,
                afinidad = oportunidad.afinidadPublico,
                organizacionId = evento.organizacionId,
                nombreEvento = evento.nombre,
                tipoEvento = evento.tipo,
                comunaId = evento.comunaId,
                ubicacion = evento.ubicacion,
                fechaInicio = formatearFecha(evento.fechaInicio),
                fechaTermino = formatearFecha(evento.fechaTermino),
                horario = evento.horario,
                publicoEsperado = evento.publicoEsperado,
                publicoObjetivo = evento.publicoObjetivo,
                asistentes = evento.asistentesEstimados.toString(),
                espacios = evento.espaciosDisponibles.toString(),
                costo = evento.costoReferencial.toString(),
                condiciones = evento.condiciones,
                requisitos = evento.requisitos,
                observaciones = oportunidad.observaciones
            )
        }
    }

    // Aplica un cambio al formulario y borra el error del campo que se está corrigiendo
    fun actualizar(
        campo: CampoOportunidad? = null,
        cambio: (FormularioOportunidadUiState) -> FormularioOportunidadUiState
    ) {
        _uiState.update { actual ->
            val nuevo = cambio(actual)
            if (campo == null) nuevo else nuevo.copy(errores = nuevo.errores - campo)
        }
    }

    fun guardar() {
        val usuarioActual = usuario ?: return
        val estado = _uiState.value
        if (estado.guardando) return

        // Si hay errores no se guarda nada y se conservan los valores ingresados
        val errores = validarFormularioOportunidad(estado, System.currentTimeMillis())
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update { it.copy(errores = emptyMap(), guardando = true) }

        viewModelScope.launch {
            try {
                val inicio = parsearFecha(estado.fechaInicio)
                    ?: throw IllegalStateException("Fecha de inicio inválida")
                val termino = parsearFecha(estado.fechaTermino)
                    ?: throw IllegalStateException("Fecha de término inválida")
                val organizacionId = estado.organizacionId
                    ?: throw IllegalStateException("Falta la organización")
                val comunaId = estado.comunaId
                    ?: throw IllegalStateException("Falta la comuna")

                val oportunidadPrevia = oportunidadOriginal
                val eventoPrevio = eventoOriginal

                // El supervisor elige responsable; el ejecutivo conserva el suyo
                val responsableFinal = when {
                    esSupervisor(usuarioActual) -> estado.responsableId
                    oportunidadPrevia != null -> oportunidadPrevia.responsableId
                    else -> usuarioActual.id
                }

                val idGuardado: Int
                if (oportunidadPrevia != null && eventoPrevio != null) {
                    // ----- Edición -----
                    repository.actualizarEvento(
                        eventoPrevio.copy(
                            organizacionId = organizacionId,
                            comunaId = comunaId,
                            nombre = estado.nombreEvento.trim(),
                            tipo = estado.tipoEvento,
                            fechaInicio = inicio,
                            fechaTermino = termino,
                            horario = estado.horario.trim(),
                            ubicacion = estado.ubicacion.trim(),
                            publicoEsperado = estado.publicoEsperado.trim(),
                            publicoObjetivo = estado.publicoObjetivo.trim(),
                            asistentesEstimados = estado.asistentes.trim().toIntOrNull() ?: 0,
                            espaciosDisponibles = estado.espacios.trim().toIntOrNull() ?: 0,
                            costoReferencial = estado.costo.trim().toIntOrNull() ?: 0,
                            condiciones = estado.condiciones.trim(),
                            requisitos = estado.requisitos.trim()
                        )
                    )
                    repository.actualizarOportunidad(
                        oportunidadPrevia.copy(
                            responsableId = responsableFinal,
                            nombre = estado.nombre.trim(),
                            descripcion = estado.descripcion.trim(),
                            fuente = estado.fuente.trim(),
                            prioridad = estado.prioridad,
                            potencialComercial = estado.potencial,
                            afinidadPublico = estado.afinidad,
                            observaciones = estado.observaciones.trim()
                        )
                    )
                    idGuardado = oportunidadPrevia.id
                } else {
                    // ----- Creación: primero el evento, luego la oportunidad -----
                    val eventoId = repository.guardarEvento(
                        Evento(
                            organizacionId = organizacionId,
                            comunaId = comunaId,
                            nombre = estado.nombreEvento.trim(),
                            tipo = estado.tipoEvento,
                            fechaInicio = inicio,
                            fechaTermino = termino,
                            horario = estado.horario.trim(),
                            ubicacion = estado.ubicacion.trim(),
                            publicoEsperado = estado.publicoEsperado.trim(),
                            publicoObjetivo = estado.publicoObjetivo.trim(),
                            asistentesEstimados = estado.asistentes.trim().toIntOrNull() ?: 0,
                            espaciosDisponibles = estado.espacios.trim().toIntOrNull() ?: 0,
                            costoReferencial = estado.costo.trim().toIntOrNull() ?: 0,
                            condiciones = estado.condiciones.trim(),
                            requisitos = estado.requisitos.trim()
                        )
                    )
                    idGuardado = repository.crearOportunidad(
                        Oportunidad(
                            eventoId = eventoId,
                            responsableId = responsableFinal,
                            nombre = estado.nombre.trim(),
                            descripcion = estado.descripcion.trim(),
                            fuente = estado.fuente.trim(),
                            prioridad = estado.prioridad,
                            potencialComercial = estado.potencial,
                            afinidadPublico = estado.afinidad,
                            observaciones = estado.observaciones.trim()
                        ),
                        usuarioActual.id
                    )
                }

                _uiState.update { it.copy(guardando = false, guardadoId = idGuardado) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No se pudo guardar la oportunidad. Inténtalo nuevamente"
                    )
                }
            }
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}