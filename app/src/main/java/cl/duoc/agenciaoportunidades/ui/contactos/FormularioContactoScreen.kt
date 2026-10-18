package cl.duoc.agenciaoportunidades.ui.contactos

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.AreaContacto
import cl.duoc.agenciaoportunidades.model.EstadoContacto
import cl.duoc.agenciaoportunidades.model.MedioContacto
import cl.duoc.agenciaoportunidades.ui.components.CampoDesplegable
import cl.duoc.agenciaoportunidades.ui.components.CampoTexto
import cl.duoc.agenciaoportunidades.util.etiqueta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContactoScreen(
    organizacionId: Int,
    contactoId: Int?,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FormularioContactoViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(organizacionId, contactoId) {
        viewModel.iniciar(organizacionId, contactoId)
    }

    LaunchedEffect(ui.guardadoId) {
        if (ui.guardadoId != null) {
            val mensaje = if (ui.esEdicion) {
                "Contacto actualizado correctamente"
            } else {
                "Contacto registrado correctamente"
            }
            Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show()
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
                title = { Text(if (ui.esEdicion) "Editar contacto" else "Nuevo contacto") },
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

            ui.noEncontrado -> {
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
                        text = "Contacto no encontrado",
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
                    Text(
                        text = ui.organizacionNombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Los datos de contacto son ficticios; no uses personas reales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CampoTexto(
                        valor = ui.nombre,
                        onValorChange = { v -> viewModel.actualizar(CampoContacto.NOMBRE) { it.copy(nombre = v) } },
                        etiqueta = "Nombre (ficticio)",
                        error = errores[CampoContacto.NOMBRE],
                        maxLength = 80
                    )
                    CampoTexto(
                        valor = ui.cargo,
                        onValorChange = { v -> viewModel.actualizar(CampoContacto.CARGO) { it.copy(cargo = v) } },
                        etiqueta = "Cargo o función",
                        error = errores[CampoContacto.CARGO],
                        maxLength = 60
                    )
                    CampoDesplegable(
                        etiqueta = "Área",
                        textoSeleccionado = ui.area.etiqueta(),
                        opciones = AreaContacto.entries.map { it.etiqueta() to it },
                        onSeleccionar = { area -> viewModel.actualizar { it.copy(area = area) } }
                    )
                    CampoTexto(
                        valor = ui.correo,
                        onValorChange = { v -> viewModel.actualizar(CampoContacto.CORREO) { it.copy(correo = v) } },
                        etiqueta = "Correo (ficticio)",
                        error = errores[CampoContacto.CORREO],
                        keyboardType = KeyboardType.Email,
                        maxLength = 80
                    )
                    CampoTexto(
                        valor = ui.telefono,
                        onValorChange = { v -> viewModel.actualizar(CampoContacto.TELEFONO) { it.copy(telefono = v) } },
                        etiqueta = "Teléfono (ficticio, 9 dígitos)",
                        error = errores[CampoContacto.TELEFONO],
                        keyboardType = KeyboardType.Phone,
                        maxLength = 12
                    )
                    CampoDesplegable(
                        etiqueta = "Medio de contacto preferente",
                        textoSeleccionado = ui.medio.etiqueta(),
                        opciones = MedioContacto.entries.map { it.etiqueta() to it },
                        onSeleccionar = { medio -> viewModel.actualizar { it.copy(medio = medio) } }
                    )
                    CampoDesplegable(
                        etiqueta = "Estado del contacto",
                        textoSeleccionado = ui.estadoContacto.etiqueta(),
                        opciones = EstadoContacto.entries.map { it.etiqueta() to it },
                        onSeleccionar = { estado -> viewModel.actualizar { it.copy(estadoContacto = estado) } }
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
                            Text(if (ui.esEdicion) "Guardar cambios" else "Registrar contacto")
                        }
                    }
                }
            }
        }
    }
}