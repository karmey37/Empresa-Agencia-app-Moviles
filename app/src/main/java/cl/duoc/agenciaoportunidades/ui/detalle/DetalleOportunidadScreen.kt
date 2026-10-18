package cl.duoc.agenciaoportunidades.ui.detalle

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Evidencia
import cl.duoc.agenciaoportunidades.model.HistorialEstado
import cl.duoc.agenciaoportunidades.model.OportunidadDetalle
import cl.duoc.agenciaoportunidades.model.SeguimientoDetalle
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.ui.components.Etiqueta
import cl.duoc.agenciaoportunidades.ui.theme.coloresApp
import cl.duoc.agenciaoportunidades.ui.theme.paraEstado
import cl.duoc.agenciaoportunidades.ui.theme.paraPrioridad
import cl.duoc.agenciaoportunidades.util.diasHasta
import cl.duoc.agenciaoportunidades.util.estadosPermitidos
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.formatearFecha
import cl.duoc.agenciaoportunidades.util.formatearFechaHora
import cl.duoc.agenciaoportunidades.util.formatearPesos
import cl.duoc.agenciaoportunidades.util.intentCalendarioEvento
import cl.duoc.agenciaoportunidades.util.intentCalendarioSeguimiento
import cl.duoc.agenciaoportunidades.util.puedeAsignarResponsable
import cl.duoc.agenciaoportunidades.util.puedeEditarOportunidad
import coil3.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOportunidadScreen(
    oportunidadId: Int,
    usuario: Usuario,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    onRegistrarSeguimiento: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleOportunidadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detalle = uiState.detalle
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogoEstado by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoAsignar by rememberSaveable { mutableStateOf(false) }
    var fotoSeleccionada by remember { mutableStateOf<Evidencia?>(null) }

    // Ruta de la foto que la cámara está escribiendo (se guarda por si Android recrea la pantalla)
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val puedeModificar = detalle != null && puedeEditarOportunidad(usuario, detalle.estado)

    // ----- Cámara: la app de cámara del sistema guarda la foto en el archivo que le indicamos -----
    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaPendiente
        rutaPendiente = null
        if (exito && ruta != null) {
            viewModel.registrarEvidencia(ruta)
        } else {
            // El usuario canceló: se borra el archivo vacío
            viewModel.cancelarCaptura(ruta)
        }
    }

    // ----- Galería: selector de fotos del sistema (no requiere permisos) -----
    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importarDesdeGaleria(uri)
        } else {
            viewModel.avisar("No se seleccionó ninguna imagen")
        }
    }

    val tomarFoto: () -> Unit = {
        try {
            val archivo = viewModel.crearArchivoEvidencia()
            rutaPendiente = archivo.absolutePath
            val uri = FileProvider.getUriForFile(
                contexto,
                "${contexto.packageName}.fileprovider",
                archivo
            )
            lanzadorCamara.launch(uri)
        } catch (e: Exception) {
            rutaPendiente?.let { File(it).delete() }
            rutaPendiente = null
            viewModel.avisar("No se pudo abrir la cámara en este dispositivo")
        }
    }

    val elegirFoto: () -> Unit = {
        lanzadorGaleria.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // ----- Calendario: abre la app de calendario con el evento ya preparado -----
    val abrirCalendario: (Intent) -> Unit = { intent ->
        try {
            contexto.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            viewModel.avisar("No hay una aplicación de calendario instalada")
        }
    }

    LaunchedEffect(oportunidadId, usuario.id) {
        viewModel.cargar(oportunidadId, usuario)
    }

    // Confirmaciones y errores de operación: se muestran una vez y se consumen
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            viewModel.consumirMensaje()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de oportunidad") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (puedeModificar) {
                        IconButton(onClick = { onEditar(oportunidadId) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar oportunidad"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.sinAcceso -> {
                MensajeCentrado(
                    icono = Icons.Filled.Lock,
                    titulo = "Sin acceso",
                    texto = "Esta oportunidad está asignada a otro ejecutivo.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            detalle == null -> {
                MensajeCentrado(
                    icono = Icons.Filled.SearchOff,
                    titulo = "Oportunidad no encontrada",
                    texto = "Es posible que haya sido eliminada.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                ContenidoDetalle(
                    detalle = detalle,
                    seguimientos = uiState.seguimientos,
                    historial = uiState.historialEstados,
                    evidencias = uiState.evidencias,
                    puedeCambiarEstado = estadosPermitidos(usuario, detalle.estado).isNotEmpty(),
                    puedeAsignar = puedeAsignarResponsable(usuario),
                    puedeRegistrar = puedeModificar,
                    onCambiarEstado = { mostrarDialogoEstado = true },
                    onAsignar = { mostrarDialogoAsignar = true },
                    onRegistrarSeguimiento = { onRegistrarSeguimiento(oportunidadId) },
                    onAgendarEvento = { abrirCalendario(intentCalendarioEvento(detalle)) },
                    onAgendarSeguimiento = { seguimiento ->
                        seguimiento.fechaProximoSeguimiento?.let { fecha ->
                            abrirCalendario(intentCalendarioSeguimiento(seguimiento, fecha))
                        }
                    },
                    onTomarFoto = tomarFoto,
                    onElegirFoto = elegirFoto,
                    onVerFoto = { fotoSeleccionada = it },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (mostrarDialogoEstado && detalle != null) {
        DialogoCambiarEstado(
            estadoActual = detalle.estado,
            opciones = estadosPermitidos(usuario, detalle.estado),
            onConfirmar = { estado, observacion ->
                mostrarDialogoEstado = false
                viewModel.cambiarEstado(estado, observacion)
            },
            onCancelar = { mostrarDialogoEstado = false }
        )
    }

    if (mostrarDialogoAsignar && detalle != null) {
        DialogoAsignar(
            ejecutivos = uiState.ejecutivos,
            responsableActualId = detalle.responsableId,
            onConfirmar = { responsableId ->
                mostrarDialogoAsignar = false
                viewModel.asignarResponsable(responsableId)
            },
            onCancelar = { mostrarDialogoAsignar = false }
        )
    }

    fotoSeleccionada?.let { foto ->
        DialogoFoto(
            evidencia = foto,
            puedeEliminar = puedeModificar,
            onEliminar = {
                fotoSeleccionada = null
                viewModel.eliminarEvidencia(foto)
            },
            onCerrar = { fotoSeleccionada = null }
        )
    }
}

@Composable
private fun ContenidoDetalle(
    detalle: OportunidadDetalle,
    seguimientos: List<SeguimientoDetalle>,
    historial: List<HistorialEstado>,
    evidencias: List<Evidencia>,
    puedeCambiarEstado: Boolean,
    puedeAsignar: Boolean,
    puedeRegistrar: Boolean,
    onCambiarEstado: () -> Unit,
    onAsignar: () -> Unit,
    onRegistrarSeguimiento: () -> Unit,
    onAgendarEvento: () -> Unit,
    onAgendarSeguimiento: (SeguimientoDetalle) -> Unit,
    onTomarFoto: () -> Unit,
    onElegirFoto: () -> Unit,
    onVerFoto: (Evidencia) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ----- Cabecera -----
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = detalle.nombre,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${detalle.organizacion} · ${detalle.comuna}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Etiqueta(
                        texto = detalle.estado.etiqueta(),
                        colores = MaterialTheme.coloresApp.paraEstado(detalle.estado)
                    )
                    Etiqueta(
                        texto = "Prioridad ${detalle.prioridad.etiqueta().lowercase()}",
                        colores = MaterialTheme.coloresApp.paraPrioridad(detalle.prioridad)
                    )
                }
            }
        }

        // ----- Acciones (según permisos del rol) -----
        Button(
            onClick = onCambiarEstado,
            enabled = puedeCambiarEstado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar estado")
        }
        if (!puedeCambiarEstado) {
            Text(
                text = "Solo el supervisor puede modificar el estado de esta oportunidad.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (puedeAsignar) {
            OutlinedButton(
                onClick = onAsignar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Asignar responsable")
            }
        }
        if (puedeRegistrar) {
            OutlinedButton(
                onClick = onRegistrarSeguimiento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar seguimiento")
            }
        }

        // ----- Evento -----
        SeccionCard(titulo = "Evento") {
            FilaDato("Nombre del evento", detalle.eventoNombre)
            FilaDato("Tipo", detalle.tipoEvento.etiqueta())
            FilaDato(
                "Fechas",
                "${formatearFecha(detalle.fechaInicio)} al ${formatearFecha(detalle.fechaTermino)}"
            )
            FilaDato("Horario", detalle.horario)
            FilaDato("Ubicación", detalle.ubicacion)
            FilaDato("Comuna y región", "${detalle.comuna}, ${detalle.region}")
            FilaDato("Público esperado", detalle.publicoEsperado)
            FilaDato("Público objetivo", detalle.publicoObjetivo)
            FilaDato("Asistentes estimados", detalle.asistentesEstimados.toString())
            FilaDato("Espacios disponibles", detalle.espaciosDisponibles.toString())
            FilaDato(
                "Costo referencial",
                if (detalle.costoReferencial == 0) "Sin costo" else formatearPesos(detalle.costoReferencial)
            )
            FilaDato("Condiciones de participación", detalle.condiciones)
            FilaDato("Requisitos", detalle.requisitos)

            OutlinedButton(
                onClick = onAgendarEvento,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar evento al calendario")
            }
        }

        // ----- Gestión comercial -----
        SeccionCard(titulo = "Gestión comercial") {
            FilaDato("Responsable", detalle.responsable ?: "Sin asignar")
            FilaDato("Organización", "${detalle.organizacion} (${detalle.rubro})")
            FilaDato("Fuente de información", detalle.fuente)
            FilaDato("Fecha de detección", formatearFecha(detalle.fechaDeteccion))
            FilaDato("Potencial comercial", detalle.potencialComercial.etiqueta())
            FilaDato("Afinidad con el público", detalle.afinidadPublico.etiqueta())
            FilaDato("Última actualización", formatearFechaHora(detalle.fechaActualizacion))
            FilaDato("Descripción", detalle.descripcion)
            FilaDato("Observaciones", detalle.observaciones)
        }

        // ----- Evidencias fotográficas -----
        SeccionCard(titulo = "Evidencias fotográficas (${evidencias.size})") {
            if (puedeRegistrar) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onTomarFoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar foto")
                    }
                    OutlinedButton(
                        onClick = onElegirFoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galería")
                    }
                }
            }
            if (evidencias.isEmpty()) {
                Text(
                    text = "Aún no hay fotografías de esta oportunidad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Toca una foto para verla en grande.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(evidencias, key = { it.id }) { evidencia ->
                        AsyncImage(
                            model = File(evidencia.rutaArchivo),
                            contentDescription = "Fotografía de evidencia",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onVerFoto(evidencia) }
                        )
                    }
                }
            }
        }

        // ----- Seguimientos -----
        SeccionCard(titulo = "Seguimientos (${seguimientos.size})") {
            if (seguimientos.isEmpty()) {
                Text(
                    text = "Aún no hay gestiones registradas para esta oportunidad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                seguimientos.forEachIndexed { indice, seguimiento ->
                    if (indice > 0) HorizontalDivider()
                    ItemSeguimiento(
                        seguimiento = seguimiento,
                        onAgendar = { onAgendarSeguimiento(seguimiento) }
                    )
                }
            }
        }

        // ----- Historial de estados -----
        SeccionCard(titulo = "Historial de estados") {
            if (historial.isEmpty()) {
                Text(
                    text = "Sin cambios de estado registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                historial.forEachIndexed { indice, cambio ->
                    if (indice > 0) HorizontalDivider()
                    ItemHistorial(cambio)
                }
            }
        }
    }
}

@Composable
private fun SeccionCard(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ItemSeguimiento(
    seguimiento: SeguimientoDetalle,
    onAgendar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "${seguimiento.tipoAccion.etiqueta()} · ${formatearFechaHora(seguimiento.fechaHora)}",
            style = MaterialTheme.typography.titleSmall
        )
        if (seguimiento.resultado.isNotBlank()) {
            Text(
                text = seguimiento.resultado,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        val quien = listOfNotNull(
            seguimiento.usuario?.let { "Por: $it" },
            seguimiento.contacto?.let { "Contacto: $it" }
        ).joinToString(" · ")
        if (quien.isNotEmpty()) {
            Text(
                text = quien,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (seguimiento.proximaAccion.isNotBlank() || seguimiento.fechaProximoSeguimiento != null) {
            val fecha = seguimiento.fechaProximoSeguimiento?.let { formatearFecha(it) } ?: "sin fecha"
            Text(
                text = "Próxima acción: ${seguimiento.proximaAccion.ifBlank { "—" }} ($fecha)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Solo se puede agendar un seguimiento que todavía no ha pasado
        val fechaProxima = seguimiento.fechaProximoSeguimiento
        if (fechaProxima != null && diasHasta(fechaProxima, System.currentTimeMillis()) >= 0) {
            TextButton(onClick = onAgendar) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agendar en el calendario")
            }
        }
    }
}

@Composable
private fun ItemHistorial(cambio: HistorialEstado) {
    val anterior = cambio.estadoAnterior
    val titulo = if (anterior == null) {
        "Inicio: ${cambio.estadoNuevo.etiqueta()}"
    } else {
        "${anterior.etiqueta()} → ${cambio.estadoNuevo.etiqueta()}"
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = titulo, style = MaterialTheme.typography.titleSmall)
        Text(
            text = formatearFechaHora(cambio.fecha),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (cambio.observaciones.isNotBlank()) {
            Text(
                text = cambio.observaciones,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MensajeCentrado(
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

@Composable
private fun FilaOpcion(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionada, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DialogoCambiarEstado(
    estadoActual: EstadoOportunidad,
    opciones: List<EstadoOportunidad>,
    onConfirmar: (EstadoOportunidad, String) -> Unit,
    onCancelar: () -> Unit
) {
    var seleccionado by remember { mutableStateOf<EstadoOportunidad?>(null) }
    var observacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cambiar estado") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Estado actual: ${estadoActual.etiqueta()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                opciones.forEach { estado ->
                    FilaOpcion(
                        texto = estado.etiqueta(),
                        seleccionada = seleccionado == estado,
                        onClick = { seleccionado = estado }
                    )
                }
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { if (it.length <= 200) observacion = it },
                    label = { Text("Observación (opcional)") },
                    supportingText = { Text("${observacion.length}/200") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { seleccionado?.let { onConfirmar(it, observacion) } },
                enabled = seleccionado != null
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogoAsignar(
    ejecutivos: List<Usuario>,
    responsableActualId: Int?,
    onConfirmar: (Int?) -> Unit,
    onCancelar: () -> Unit
) {
    var seleccionado by remember { mutableStateOf(responsableActualId) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Asignar responsable") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                FilaOpcion(
                    texto = "Sin asignar",
                    seleccionada = seleccionado == null,
                    onClick = { seleccionado = null }
                )
                ejecutivos.forEach { ejecutivo ->
                    FilaOpcion(
                        texto = "${ejecutivo.nombre} (${ejecutivo.zona})",
                        seleccionada = seleccionado == ejecutivo.id,
                        onClick = { seleccionado = ejecutivo.id }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(seleccionado) }) {
                Text("Asignar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

// Vista previa ampliada de una evidencia, con opción de eliminarla
@Composable
private fun DialogoFoto(
    evidencia: Evidencia,
    puedeEliminar: Boolean,
    onEliminar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Evidencia · ${formatearFechaHora(evidencia.fecha)}") },
        text = {
            AsyncImage(
                model = File(evidencia.rutaArchivo),
                contentDescription = "Fotografía ampliada",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar")
            }
        },
        dismissButton = {
            if (puedeEliminar) {
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}