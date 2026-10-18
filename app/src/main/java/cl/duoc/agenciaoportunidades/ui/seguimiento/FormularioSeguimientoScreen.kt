package cl.duoc.agenciaoportunidades.ui.seguimiento

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.TipoAccion
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.ui.components.CampoDesplegable
import cl.duoc.agenciaoportunidades.ui.components.CampoTexto
import cl.duoc.agenciaoportunidades.util.etiqueta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioSeguimientoScreen(
    oportunidadId: Int,
    usuario: Usuario,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioSeguimientoViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(usuario.id, oportunidadId) {
        viewModel.iniciar(usuario, oportunidadId)
    }

    // Confirmación de éxito y regreso al detalle
    LaunchedEffect(ui.guardadoId) {
        if (ui.guardadoId != null) {
            Toast.makeText(contexto, "Seguimiento registrado correctamente", Toast.LENGTH_SHORT).show()
            onGuardado()
        }
    }

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
                title = { Text("Registrar seguimiento") },
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
                MensajeSeguimiento(
                    icono = Icons.Filled.Lock,
                    titulo = "No puedes registrar seguimientos",
                    texto = "La oportunidad está cerrada o asignada a otra persona.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            ui.noEncontrada -> {
                MensajeSeguimiento(
                    icono = Icons.Filled.SearchOff,
                    titulo = "Oportunidad no encontrada",
                    texto = "Es posible que haya sido eliminada.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                ContenidoSeguimiento(
                    ui = ui,
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ContenidoSeguimiento(
    ui: FormularioSeguimientoUiState,
    viewModel: FormularioSeguimientoViewModel,
    modifier: Modifier = Modifier
) {
    val errores = ui.errores

    val opcionesContacto = buildList<Pair<String, Int?>> {
        add("Sin contacto" to null)
        ui.contactos.forEach { add("${it.nombre} · ${it.cargo}" to it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = ui.oportunidadNombre,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Registra la gestión realizada y, si corresponde, programa la siguiente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ================= Gestión realizada =================
        TituloSeccionSeguimiento("Gestión realizada")

        CampoDesplegable(
            etiqueta = "Tipo de acción",
            textoSeleccionado = ui.tipoAccion.etiqueta(),
            opciones = TipoAccion.entries.map { it.etiqueta() to it },
            onSeleccionar = { tipo -> viewModel.actualizar { it.copy(tipoAccion = tipo) } }
        )
        CampoDesplegable(
            etiqueta = "Contacto (opcional)",
            textoSeleccionado = ui.contactos.find { it.id == ui.contactoId }?.nombre
                ?: "Sin contacto",
            opciones = opcionesContacto,
            onSeleccionar = { id -> viewModel.actualizar { it.copy(contactoId = id) } }
        )
        CampoTexto(
            valor = ui.fecha,
            onValorChange = { v -> viewModel.actualizar(CampoSeguimiento.FECHA) { it.copy(fecha = v) } },
            etiqueta = "Fecha de la gestión (dd/mm/aaaa)",
            error = errores[CampoSeguimiento.FECHA],
            maxLength = 10
        )
        CampoTexto(
            valor = ui.resultado,
            onValorChange = { v -> viewModel.actualizar(CampoSeguimiento.RESULTADO) { it.copy(resultado = v) } },
            etiqueta = "Resultado de la gestión",
            error = errores[CampoSeguimiento.RESULTADO],
            singleLine = false,
            maxLength = 200
        )

        // ================= Próximo seguimiento =================
        TituloSeccionSeguimiento("Próximo seguimiento (opcional)")

        CampoTexto(
            valor = ui.proximaAccion,
            onValorChange = { v -> viewModel.actualizar(CampoSeguimiento.PROXIMA_ACCION) { it.copy(proximaAccion = v) } },
            etiqueta = "Próxima acción (ej: Llamar para confirmar)",
            error = errores[CampoSeguimiento.PROXIMA_ACCION],
            maxLength = 100
        )
        CampoTexto(
            valor = ui.fechaProxima,
            onValorChange = { v -> viewModel.actualizar(CampoSeguimiento.FECHA_PROXIMA) { it.copy(fechaProxima = v) } },
            etiqueta = "Fecha del próximo seguimiento (dd/mm/aaaa)",
            error = errores[CampoSeguimiento.FECHA_PROXIMA],
            maxLength = 10
        )
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
                Text("Registrar seguimiento")
            }
        }
    }
}

@Composable
private fun TituloSeccionSeguimiento(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun MensajeSeguimiento(
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