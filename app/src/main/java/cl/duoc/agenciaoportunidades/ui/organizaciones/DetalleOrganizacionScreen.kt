package cl.duoc.agenciaoportunidades.ui.organizaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.Contacto
import cl.duoc.agenciaoportunidades.model.Evento
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrganizacionScreen(
    organizacionId: Int,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleOrganizacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val organizacion = uiState.organizacion

    LaunchedEffect(organizacionId) {
        viewModel.cargar(organizacionId)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Organización") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (organizacion != null) {
                        IconButton(onClick = { onEditar(organizacionId) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar organización"
                            )
                        }
                    }
                }
            )
        }
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

            organizacion == null -> {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = organizacion.nombre,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "${organizacion.tipo.etiqueta()} · ${organizacion.rubro}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SeccionOrganizacion(titulo = "Datos") {
                        FilaDatoOrganizacion(
                            "Comuna y región",
                            uiState.comuna?.let { "${it.nombre}, ${it.region}" } ?: ""
                        )
                        FilaDatoOrganizacion("Sitio web", organizacion.sitioWeb)
                        FilaDatoOrganizacion("Público asociado", organizacion.publicoAsociado)
                        FilaDatoOrganizacion("Observaciones", organizacion.observaciones)
                    }

                    SeccionOrganizacion(titulo = "Contactos (${uiState.contactos.size})") {
                        if (uiState.contactos.isEmpty()) {
                            Text(
                                text = "Esta organización aún no tiene contactos registrados.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.contactos.forEachIndexed { indice, contacto ->
                                if (indice > 0) HorizontalDivider()
                                ItemContacto(contacto)
                            }
                        }
                    }

                    SeccionOrganizacion(titulo = "Eventos relacionados (${uiState.eventos.size})") {
                        if (uiState.eventos.isEmpty()) {
                            Text(
                                text = "No hay eventos registrados para esta organización.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.eventos.forEachIndexed { indice, evento ->
                                if (indice > 0) HorizontalDivider()
                                ItemEvento(evento)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionOrganizacion(
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
private fun FilaDatoOrganizacion(etiqueta: String, valor: String) {
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
private fun ItemContacto(contacto: Contacto) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = contacto.nombre, style = MaterialTheme.typography.titleSmall)
        Text(
            text = "${contacto.cargo} · ${contacto.area.etiqueta()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${contacto.correo} · ${contacto.telefono}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Prefiere: ${contacto.medioPreferente.etiqueta()} · ${contacto.estado.etiqueta()}",
            style = MaterialTheme.typography.bodySmall
        )
        contacto.fechaUltimoContacto?.let { fecha ->
            Text(
                text = "Último contacto: ${formatearFecha(fecha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ItemEvento(evento: Evento) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = evento.nombre, style = MaterialTheme.typography.titleSmall)
        Text(
            text = "${evento.tipo.etiqueta()} · ${formatearFecha(evento.fechaInicio)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}