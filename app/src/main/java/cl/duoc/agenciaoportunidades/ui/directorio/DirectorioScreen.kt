package cl.duoc.agenciaoportunidades.ui.directorio

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.data.remote.OrganizacionDto
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion
import cl.duoc.agenciaoportunidades.ui.components.CampoDesplegable
import cl.duoc.agenciaoportunidades.ui.components.CampoTexto
import cl.duoc.agenciaoportunidades.util.etiqueta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorioScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DirectorioViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lista = ui.filtradas

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            viewModel.consumirMensaje()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Directorio en servidor") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargar() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.abrirNuevo() }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva organización")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = ui.busqueda,
                onValueChange = { viewModel.cambiarBusqueda(it) },
                label = { Text("Buscar por nombre, rubro o comuna") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (ui.busqueda.isNotEmpty()) {
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

            Text(
                text = "${lista.size} organizaciones en el servidor",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            when {
                ui.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                lista.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No hay organizaciones para mostrar",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            text = "Revisa que la API esté encendida o crea una con el botón +.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista, key = { it.id ?: it.nombre }) { org ->
                            TarjetaDirectorio(
                                org = org,
                                onEditar = { viewModel.abrirEditar(org) },
                                onEliminar = { viewModel.pedirEliminar(org) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (ui.formularioAbierto) {
        FormularioDirectorio(ui = ui, viewModel = viewModel)
    }

    ui.aEliminar?.let { org ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminar() },
            title = { Text("Eliminar organización") },
            text = {
                Text("¿Seguro que quieres eliminar \"${org.nombre}\" del servidor? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarEliminar() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEliminar() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaDirectorio(
    org: OrganizacionDto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tipoTexto = runCatching { TipoOrganizacion.valueOf(org.tipo).etiqueta() }
        .getOrDefault(org.tipo)

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = org.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$tipoTexto · ${org.rubro}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = org.comuna,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FormularioDirectorio(
    ui: DirectorioUiState,
    viewModel: DirectorioViewModel
) {
    val errores = ui.errores

    AlertDialog(
        onDismissRequest = { if (!ui.guardando) viewModel.cerrarFormulario() },
        title = {
            Text(if (ui.editandoId == null) "Nueva organización" else "Editar organización")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CampoTexto(
                    valor = ui.nombre,
                    onValorChange = { v ->
                        viewModel.actualizar(CampoDirectorio.NOMBRE) { it.copy(nombre = v) }
                    },
                    etiqueta = "Nombre",
                    error = errores[CampoDirectorio.NOMBRE],
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
                    onValorChange = { v ->
                        viewModel.actualizar(CampoDirectorio.RUBRO) { it.copy(rubro = v) }
                    },
                    etiqueta = "Rubro",
                    error = errores[CampoDirectorio.RUBRO],
                    maxLength = 60
                )
                CampoTexto(
                    valor = ui.comuna,
                    onValorChange = { v ->
                        viewModel.actualizar(CampoDirectorio.COMUNA) { it.copy(comuna = v) }
                    },
                    etiqueta = "Comuna",
                    error = errores[CampoDirectorio.COMUNA],
                    maxLength = 60
                )
                CampoTexto(
                    valor = ui.sitioWeb,
                    onValorChange = { v ->
                        viewModel.actualizar(CampoDirectorio.SITIO_WEB) { it.copy(sitioWeb = v) }
                    },
                    etiqueta = "Sitio web (opcional)",
                    error = errores[CampoDirectorio.SITIO_WEB],
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
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.guardar() },
                enabled = !ui.guardando
            ) {
                if (ui.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.cerrarFormulario() },
                enabled = !ui.guardando
            ) {
                Text("Cancelar")
            }
        }
    )
}