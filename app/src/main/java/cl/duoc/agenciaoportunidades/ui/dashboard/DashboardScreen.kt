package cl.duoc.agenciaoportunidades.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.agenciaoportunidades.model.PerfilUsuario
import cl.duoc.agenciaoportunidades.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "Hola, ${usuario.nombre}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (usuario.perfil == PerfilUsuario.SUPERVISOR) {
                    "Perfil: Supervisor Comercial"
                } else {
                    "Perfil: Ejecutivo Comercial"
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}