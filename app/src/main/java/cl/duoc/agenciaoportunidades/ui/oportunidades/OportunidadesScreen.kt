package cl.duoc.agenciaoportunidades.ui.oportunidades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.ui.components.Etiqueta
import cl.duoc.agenciaoportunidades.ui.theme.coloresApp
import cl.duoc.agenciaoportunidades.ui.theme.paraEstado
import cl.duoc.agenciaoportunidades.ui.theme.paraPrioridad
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.formatearFecha
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OportunidadesScreen(
    usuario: Usuario,
    onAbrirOportunidad: (Int) -> Unit,
    onNuevaOportunidad: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OportunidadesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lista = uiState.filtradas

    LaunchedEffect(usuario.id) {
        viewModel.cargar(usuario)
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Oportunidades") }) },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaOportunidad) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva oportunidad")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ----- Búsqueda -----
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = { viewModel.cambiarBusqueda(it) },
                label = { Text("Buscar por nombre, organización o comuna") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.cambiarBusqueda("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ----- Filtro por estado -----
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.estadoFiltro == null,
                        onClick = { viewModel.cambiarEstadoFiltro(null) },
                        label = { Text("Todos los estados") }
                    )
                }
                items(EstadoOportunidad.entries) { estado ->
                    FilterChip(
                        selected = uiState.estadoFiltro == estado,
                        onClick = { viewModel.cambiarEstadoFiltro(estado) },
                        label = { Text(estado.etiqueta()) }
                    )
                }
            }

            // ----- Filtro por prioridad -----
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.prioridadFiltro == null,
                        onClick = { viewModel.cambiarPrioridadFiltro(null) },
                        label = { Text("Todas las prioridades") }
                    )
                }
                items(Prioridad.entries) { prioridad ->
                    FilterChip(
                        selected = uiState.prioridadFiltro == prioridad,
                        onClick = { viewModel.cambiarPrioridadFiltro(prioridad) },
                        label = { Text("Prioridad ${prioridad.etiqueta().lowercase()}") }
                    )
                }
            }

            Text(
                text = "${lista.size} oportunidades",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ----- Contenido: carga, vacio o lista -----
            when {
                uiState.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                lista.isEmpty() -> {
                    EstadoVacio(modifier = Modifier.fillMaxSize())
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista, key = { it.id }) { oportunidad ->
                            // ANIMACIÓN: las tarjetas se reubican suavemente al filtrar
                            TarjetaOportunidad(
                                oportunidad = oportunidad,
                                onClick = { onAbrirOportunidad(oportunidad.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOportunidad(
    oportunidad: OportunidadResumen,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorEstado = MaterialTheme.coloresApp.paraEstado(oportunidad.estado)
    val colorPrioridad = MaterialTheme.coloresApp.paraPrioridad(oportunidad.prioridad)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = oportunidad.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${oportunidad.organizacion} · ${oportunidad.comuna}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Evento: ${formatearFecha(oportunidad.fechaInicio)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Etiqueta(texto = oportunidad.estado.etiqueta(), colores = colorEstado)
                Etiqueta(
                    texto = "Prioridad ${oportunidad.prioridad.etiqueta().lowercase()}",
                    colores = colorPrioridad
                )
            }
            Text(
                text = "Responsable: ${oportunidad.responsable ?: "Sin asignar"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EstadoVacio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
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
            text = "No hay oportunidades que coincidan",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Prueba con otra búsqueda o quita algún filtro.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}