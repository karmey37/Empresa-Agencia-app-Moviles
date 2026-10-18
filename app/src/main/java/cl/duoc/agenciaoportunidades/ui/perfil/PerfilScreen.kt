package cl.duoc.agenciaoportunidades.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.ui.components.Etiqueta
import cl.duoc.agenciaoportunidades.ui.theme.coloresApp
import cl.duoc.agenciaoportunidades.util.esSupervisor
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.formatearFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val supervisor = esSupervisor(usuario)
    val colores = MaterialTheme.coloresApp

    val permisos = if (supervisor) {
        listOf(
            "Ver todas las oportunidades del equipo",
            "Asignar y reasignar responsables",
            "Confirmar, descartar y cerrar oportunidades",
            "Consultar las gestiones atrasadas de todo el equipo"
        )
    } else {
        listOf(
            "Ver las oportunidades que tienes asignadas",
            "Avanzar el estado de tus oportunidades",
            "No puedes confirmar ni descartar de forma definitiva",
            "No puedes asignar ni reasignar responsables"
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Perfil") }) },
        bottomBar = bottomBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ----- Cabecera -----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Etiqueta(
                        texto = usuario.perfil.etiqueta(),
                        colores = if (supervisor) colores.nuevo else colores.info
                    )
                }
            }

            // ----- Datos de la cuenta -----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Datos de la cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FilaDato("Correo", usuario.correo)
                    FilaDato("Área", usuario.area)
                    FilaDato("Zona", usuario.zona)
                    FilaDato("Estado de la cuenta", if (usuario.activo) "Activa" else "Inactiva")
                    FilaDato("Cuenta creada", formatearFecha(usuario.fechaCreacion))
                }
            }

            // ----- Permisos del rol -----
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Qué puedes hacer con este perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    permisos.forEach { permiso ->
                        Text(
                            text = "•  $permiso",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
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