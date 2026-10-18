package cl.duoc.agenciaoportunidades.ui.organizaciones

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion
import cl.duoc.agenciaoportunidades.ui.components.CampoDesplegable
import cl.duoc.agenciaoportunidades.ui.components.CampoTexto
import cl.duoc.agenciaoportunidades.util.etiqueta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioOrganizacionScreen(
    organizacionId: Int?,
    onVolver: () -> Unit,
    onGuardado: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioOrganizacionViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(organizacionId) {
        viewModel.iniciar(organizacionId)
    }

    LaunchedEffect(ui.guardadoId) {
        ui.guardadoId?.let { id ->
            val mensaje = if (ui.esEdicion) {
                "Organización actualizada correctamente"
            } else {
                "Organización registrada correctamente"
            }
            Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show()
            onGuardado(id, ui.esEdicion)
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
                title = { Text(if (ui.esEdicion) "Editar organización" else "Nueva organización") },
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

            ui.noEncontrada -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Organización no encontrada",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            else -> {
                val errores = ui.errores

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CampoTexto(
                        valor = ui.nombre,
                        onValorChange = { v -> viewModel.actualizar(CampoOrganizacion.NOMBRE) { it.copy(nombre = v) } },
                        etiqueta = "Nombre de la organización",
                        error = errores[CampoOrganizacion.NOMBRE],
                        maxLength = 100
                    )
                    CampoDesplegable(
                        etiqueta = "Tipo de organización",
                        textoSeleccionado = ui.tipo.etiqueta(),
                        opciones = TipoOrganizacion.entries.map { it.etiqueta() to it },
                        onSeleccionar = { tipo -> viewModel.actualizar { it.copy(tipo = tipo) } }
                    )
                    CampoTexto(
                        valor = ui.rubro,
                        onValorChange = { v -> viewModel.actualizar(CampoOrganizacion.RUBRO) { it.copy(rubro = v) } },
                        etiqueta = "Rubro (ej: Retail, Educación)",
                        error = errores[CampoOrganizacion.RUBRO],
                        maxLength = 60
                    )
                    CampoDesplegable(
                        etiqueta = "Comuna",
                        textoSeleccionado = ui.comunas.find { it.id == ui.comunaId }?.nombre ?: "",
                        opciones = ui.comunas.map { "${it.nombre} (${it.region})" to it.id },
                        onSeleccionar = { id ->
                            viewModel.actualizar(CampoOrganizacion.COMUNA) { it.copy(comunaId = id) }
                        },
                        error = errores[CampoOrganizacion.COMUNA]
                    )
                    CampoTexto(
                        valor = ui.sitioWeb,
                        onValorChange = { v -> viewModel.actualizar(CampoOrganizacion.SITIO_WEB) { it.copy(sitioWeb = v) } },
                        etiqueta = "Sitio web (opcional)",
                        error = errores[CampoOrganizacion.SITIO_WEB],
                        maxLength = 100
                    )
                    CampoTexto(
                        valor = ui.publicoAsociado,
                        onValorChange = { v -> viewModel.actualizar { it.copy(publicoAsociado = v) } },
                        etiqueta = "Público asociado (opcional)",
                        maxLength = 100
                    )
                    CampoTexto(
                        valor = ui.observaciones,
                        onValorChange = { v -> viewModel.actualizar { it.copy(observaciones = v) } },
                        etiqueta = "Observaciones (opcional)",
                        singleLine = false,
                        maxLength = 300
                    )

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
                            Text(if (ui.esEdicion) "Guardar cambios" else "Registrar organización")
                        }
                    }
                }
            }
        }
    }
}