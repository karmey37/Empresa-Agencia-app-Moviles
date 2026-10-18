package cl.duoc.agenciaoportunidades.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

private data class DestinoBarra(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

@Composable
fun BarraInferior(
    rutaActual: String,
    onNavegar: (String) -> Unit
) {
    val destinos = listOf(
        DestinoBarra(Rutas.DASHBOARD, "Inicio", Icons.Filled.Home),
        DestinoBarra(Rutas.OPORTUNIDADES, "Oportunidades", Icons.AutoMirrored.Filled.FormatListBulleted),
        DestinoBarra(Rutas.ORGANIZACIONES, "Organizaciones", Icons.Filled.Business),
        DestinoBarra(Rutas.PERFIL, "Perfil", Icons.Filled.Person)
    )

    NavigationBar {
        destinos.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = { onNavegar(destino.ruta) },
                icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                label = { Text(destino.titulo) }
            )
        }
    }
}