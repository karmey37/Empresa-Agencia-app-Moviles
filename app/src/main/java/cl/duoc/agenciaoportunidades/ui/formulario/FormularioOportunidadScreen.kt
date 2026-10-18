package cl.duoc.agenciaoportunidades.ui.formulario

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.TipoEvento
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.ui.components.CampoDesplegable
import cl.duoc.agenciaoportunidades.ui.components.CampoTexto
import cl.duoc.agenciaoportunidades.util.esSupervisor
import cl.duoc.agenciaoportunidades.util.etiqueta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioOportunidadScreen(
    usuario: Usuario,
    oportunidadId: Int?,
    onVolver: () -> Unit,
    onGuardado: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioOportunidadViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(usuario.id, oportunidadId) {
        viewModel.iniciar(usuario, oportunidadId)
    }

    // Confirmación de éxito y paso a la pantalla siguiente
    LaunchedEffect(ui.guardadoId) {
        ui.guardadoId?.let { id ->
            val mensaje = if (ui.esEdicion) {
                "Oportunidad actualizada correctamente"
            } else {
                "Oportunidad creada correctamente"
            }
            Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show()
            onGuardado(id, ui.esEdicion)
        }
    }

    // Errores de operación (por ejemplo, falla al guardar)
    LaunchedEffect(ui.mensajeError) {
        ui.mensajeError?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            viewModel.consumirMensaje()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (ui.esEdicion) "Editar oportunidad" else "Nueva oportunidad") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            ui.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            ui.sinPermiso -> {
                MensajeFormulario(
                    icono = Icons.Filled.Lock,
                    titulo = "No puedes editar esta oportunidad",
                    texto = "Está cerrada o asignada a otra persona.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            ui.noEncontrada -> {
                MensajeFormulario(
                    icono = Icons.Filled.SearchOff,
                    titulo = "Oportunidad no encontrada",
                    texto = "Es posible que haya sido eliminada.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                ContenidoFormulario(
                    ui = ui,
                    supervisor = esSupervisor(usuario),
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ContenidoFormulario(
    ui: FormularioOportunidadUiState,
    supervisor: Boolean,
    viewModel: FormularioOportunidadViewModel,
    modifier: Modifier = Modifier
) {
    val errores = ui.errores

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ================= Datos de la oportunidad =================
        TituloSeccion("Datos de la oportunidad")

        CampoTexto(
            valor = ui.nombre,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.NOMBRE) { it.copy(nombre = v) } },
            etiqueta = "Nombre de la oportunidad",
            error = errores[CampoOportunidad.NOMBRE],
            maxLength = 100
        )
        CampoTexto(
            valor = ui.descripcion,
            onValorChange = { v -> viewModel.actualizar { it.copy(descripcion = v) } },
            etiqueta = "Descripción (opcional)",
            singleLine = false,
            maxLength = 300
        )
        CampoTexto(
            valor = ui.fuente,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.FUENTE) { it.copy(fuente = v) } },
            etiqueta = "Fuente de información (ej: Redes sociales)",
            error = errores[CampoOportunidad.FUENTE],
            maxLength = 60
        )

        if (supervisor) {
            val opcionesResponsable = buildList<Pair<String, Int?>> {
                add("Sin asignar" to null)
                ui.ejecutivos.forEach { add("${it.nombre} (${it.zona})" to it.id) }
            }
            CampoDesplegable(
                etiqueta = "Responsable",
                textoSeleccionado = ui.ejecutivos.find { it.id == ui.responsableId }?.nombre
                    ?: "Sin asignar",
                opciones = opcionesResponsable,
                onSeleccionar = { id -> viewModel.actualizar { it.copy(responsableId = id) } }
            )
        }

        SelectorNivel(
            titulo = "Prioridad",
            seleccionada = ui.prioridad,
            onSeleccion = { p -> viewModel.actualizar { it.copy(prioridad = p) } }
        )
        SelectorNivel(
            titulo = "Potencial comercial estimado",
            seleccionada = ui.potencial,
            onSeleccion = { p -> viewModel.actualizar { it.copy(potencial = p) } }
        )
        SelectorNivel(
            titulo = "Afinidad con el público objetivo",
            seleccionada = ui.afinidad,
            onSeleccion = { p -> viewModel.actualizar { it.copy(afinidad = p) } }
        )

        // ================= Evento =================
        TituloSeccion("Evento")

        CampoDesplegable(
            etiqueta = "Organización responsable",
            textoSeleccionado = ui.organizaciones.find { it.id == ui.organizacionId }?.nombre ?: "",
            opciones = ui.organizaciones.map { it.nombre to it.id },
            onSeleccionar = { id ->
                viewModel.actualizar(CampoOportunidad.ORGANIZACION) { it.copy(organizacionId = id) }
            },
            error = errores[CampoOportunidad.ORGANIZACION]
        )
        CampoTexto(
            valor = ui.nombreEvento,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.EVENTO) { it.copy(nombreEvento = v) } },
            etiqueta = "Nombre del evento",
            error = errores[CampoOportunidad.EVENTO],
            maxLength = 100
        )
        CampoDesplegable(
            etiqueta = "Tipo de evento",
            textoSeleccionado = ui.tipoEvento.etiqueta(),
            opciones = TipoEvento.entries.map { it.etiqueta() to it },
            onSeleccionar = { tipo -> viewModel.actualizar { it.copy(tipoEvento = tipo) } }
        )
        CampoDesplegable(
            etiqueta = "Comuna",
            textoSeleccionado = ui.comunas.find { it.id == ui.comunaId }?.nombre ?: "",
            opciones = ui.comunas.map { "${it.nombre} (${it.region})" to it.id },
            onSeleccionar = { id ->
                viewModel.actualizar(CampoOportunidad.COMUNA) { it.copy(comunaId = id) }
            },
            error = errores[CampoOportunidad.COMUNA]
        )
        CampoTexto(
            valor = ui.ubicacion,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.UBICACION) { it.copy(ubicacion = v) } },
            etiqueta = "Ubicación general (ej: Plaza principal)",
            error = errores[CampoOportunidad.UBICACION],
            maxLength = 100
        )
        CampoTexto(
            valor = ui.fechaInicio,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.FECHA_INICIO) { it.copy(fechaInicio = v) } },
            etiqueta = "Fecha de inicio (dd/mm/aaaa)",
            error = errores[CampoOportunidad.FECHA_INICIO],
            maxLength = 10
        )
        CampoTexto(
            valor = ui.fechaTermino,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.FECHA_TERMINO) { it.copy(fechaTermino = v) } },
            etiqueta = "Fecha de término (dd/mm/aaaa)",
            error = errores[CampoOportunidad.FECHA_TERMINO],
            maxLength = 10
        )
        CampoTexto(
            valor = ui.horario,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.HORARIO) { it.copy(horario = v) } },
            etiqueta = "Horario (HH:mm - HH:mm)",
            error = errores[CampoOportunidad.HORARIO],
            maxLength = 13
        )
        CampoTexto(
            valor = ui.publicoEsperado,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.PUBLICO_ESPERADO) { it.copy(publicoEsperado = v) } },
            etiqueta = "Público esperado",
            error = errores[CampoOportunidad.PUBLICO_ESPERADO],
            maxLength = 100
        )
        CampoTexto(
            valor = ui.publicoObjetivo,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.PUBLICO_OBJETIVO) { it.copy(publicoObjetivo = v) } },
            etiqueta = "Público objetivo",
            error = errores[CampoOportunidad.PUBLICO_OBJETIVO],
            maxLength = 100
        )
        CampoTexto(
            valor = ui.asistentes,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.ASISTENTES) { it.copy(asistentes = v) } },
            etiqueta = "Asistentes estimados",
            error = errores[CampoOportunidad.ASISTENTES],
            keyboardType = KeyboardType.Number,
            maxLength = 7
        )
        CampoTexto(
            valor = ui.espacios,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.ESPACIOS) { it.copy(espacios = v) } },
            etiqueta = "Espacios disponibles",
            error = errores[CampoOportunidad.ESPACIOS],
            keyboardType = KeyboardType.Number,
            maxLength = 4
        )
        CampoTexto(
            valor = ui.costo,
            onValorChange = { v -> viewModel.actualizar(CampoOportunidad.COSTO) { it.copy(costo = v) } },
            etiqueta = "Costo referencial en pesos (ficticio)",
            error = errores[CampoOportunidad.COSTO],
            keyboardType = KeyboardType.Number,
            maxLength = 9
        )
        CampoTexto(
            valor = ui.condiciones,
            onValorChange = { v -> viewModel.actualizar { it.copy(condiciones = v) } },
            etiqueta = "Condiciones de participación (opcional)",
            singleLine = false,
            maxLength = 300
        )
        CampoTexto(
            valor = ui.requisitos,
            onValorChange = { v -> viewModel.actualizar { it.copy(requisitos = v) } },
            etiqueta = "Requisitos (opcional)",
            singleLine = false,
            maxLength = 300
        )

        // ================= Observaciones =================
        TituloSeccion("Observaciones")

        CampoTexto(
            valor = ui.observaciones,
            onValorChange = { v -> viewModel.actualizar { it.copy(observaciones = v) } },
            etiqueta = "Observaciones (opcional)",
            singleLine = false,
            maxLength = 300
        )

        // ================= Guardar =================
        if (errores.isNotEmpty()) {
            Text(
                text = "Revisa los campos marcados en rojo antes de guardar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = { viewModel.guardar() },
            enabled = !ui.guardando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            if (ui.guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (ui.esEdicion) "Guardar cambios" else "Crear oportunidad")
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

// Selector de Baja / Media / Alta
@Composable
private fun SelectorNivel(
    titulo: String,
    seleccionada: Prioridad,
    onSeleccion: (Prioridad) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Prioridad.entries.forEach { nivel ->
                FilterChip(
                    selected = seleccionada == nivel,
                    onClick = { onSeleccion(nivel) },
                    label = { Text(nivel.etiqueta()) }
                )
            }
        }
    }
}

@Composable
private fun MensajeFormulario(
    icono: ImageVector,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}