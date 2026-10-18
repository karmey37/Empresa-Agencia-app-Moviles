package cl.duoc.agenciaoportunidades.ui.equipo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.ui.components.Etiqueta
import cl.duoc.agenciaoportunidades.ui.theme.coloresApp
import cl.duoc.agenciaoportunidades.ui.theme.paraPrioridad
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoScreen(
    usuario: Usuario,
    onAbrirOportunidad: (Int) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EquipoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var oportunidadAAsignar by remember { mutableStateOf<OportunidadResumen?>(null) }

    LaunchedEffect(usuario.id) {
        viewModel.cargar(usuario)
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            viewModel.consumirMensaje()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Equipo comercial") }) },
        bottomBar = bottomBar,
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Solo para supervisores",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.totalOportunidades} oportunidades en total · " +
                                    "${uiState.totalAtrasadas} gestiones atrasadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Text(
                            text = "Ejecutivos",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(uiState.ejecutivos, key = { "ejecutivo-${it.ejecutivo.id}" }) { resumen ->
                        TarjetaEjecutivo(resumen)
                    }

                    item {
                        Text(
                            text = "Sin asignar (${uiState.sinAsignar.size})",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (uiState.sinAsignar.isEmpty()) {
                        item {
                            Text(
                                text = "Todas las oportunidades abiertas tienen responsable.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.sinAsignar, key = { "sin-${it.id}" }) { oportunidad ->
                            TarjetaSinAsignar(
                                oportunidad = oportunidad,
                                onAbrir = { onAbrirOportunidad(oportunidad.id) },
                                onAsignar = { oportunidadAAsignar = oportunidad }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para asignar un responsable a una oportunidad sin asignar
    oportunidadAAsignar?.let { oportunidad ->
        DialogoAsignarEquipo(
            nombreOportunidad = oportunidad.nombre,
            ejecutivos = uiState.ejecutivos.map { it.ejecutivo },
            onConfirmar = { responsableId ->
                oportunidadAAsignar = null
                viewModel.asignar(oportunidad.id, responsableId)
            },
            onCancelar = { oportunidadAAsignar = null }
        )
    }
}

@Composable
private fun TarjetaEjecutivo(resumen: ResumenEjecutivo) {
    val colores = MaterialTheme.coloresApp

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = resumen.ejecutivo.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = resumen.ejecutivo.zona,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DatoNumerico("Asignadas", resumen.asignadas)
                DatoNumerico("Abiertas", resumen.abiertas)
                DatoNumerico("Alta prioridad", resumen.altaPrioridad)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Etiqueta(
                        texto = resumen.atrasadas.toString(),
                        colores = if (resumen.atrasadas > 0) colores.peligro else colores.neutro
                    )
                    Text(
                        text = "Atrasadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DatoNumerico(titulo: String, valor: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TarjetaSinAsignar(
    oportunidad: OportunidadResumen,
    onAbrir: () -> Unit,
    onAsignar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAbrir)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = oportunidad.nombre, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "${oportunidad.organizacion} · ${oportunidad.comuna} · " +
                        formatearFecha(oportunidad.fechaInicio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Etiqueta(
                    texto = "Prioridad ${oportunidad.prioridad.etiqueta().lowercase()}",
                    colores = MaterialTheme.coloresApp.paraPrioridad(oportunidad.prioridad)
                )
                TextButton(onClick = onAsignar) {
                    Text("Asignar")
                }
            }
        }
    }
}

@Composable
private fun DialogoAsignarEquipo(
    nombreOportunidad: String,
    ejecutivos: List<Usuario>,
    onConfirmar: (Int) -> Unit,
    onCancelar: () -> Unit
) {
    var seleccionado by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Asignar responsable") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = nombreOportunidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ejecutivos.forEach { ejecutivo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = seleccionado == ejecutivo.id,
                                onClick = { seleccionado = ejecutivo.id },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = seleccionado == ejecutivo.id, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${ejecutivo.nombre} (${ejecutivo.zona})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { seleccionado?.let { onConfirmar(it) } },
                enabled = seleccionado != null
            ) {
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