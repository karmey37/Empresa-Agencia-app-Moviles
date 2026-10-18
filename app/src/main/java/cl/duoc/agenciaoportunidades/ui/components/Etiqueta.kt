package cl.duoc.agenciaoportunidades.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.agenciaoportunidades.ui.theme.ParDeColor

@Composable
fun Etiqueta(
    texto: String,
    colores: ParDeColor,
    modifier: Modifier = Modifier
) {
    // ANIMACIÓN: al cambiar el estado, la pastilla pasa de un color al otro suavemente
    val fondo by animateColorAsState(targetValue = colores.fondo, label = "fondoEtiqueta")
    val textoColor by animateColorAsState(targetValue = colores.texto, label = "textoEtiqueta")

    Surface(
        color = fondo,
        contentColor = textoColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}