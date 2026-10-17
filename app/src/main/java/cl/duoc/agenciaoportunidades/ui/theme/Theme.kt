package cl.duoc.agenciaoportunidades.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val EsquemaClaro = lightColorScheme(
    primary = SolBlue,
    onPrimary = SolBase3,
    primaryContainer = SolBlueContenedorClaro,
    onPrimaryContainer = SolBase02,
    secondary = SolCyan,
    onSecondary = SolBase03,
    secondaryContainer = SolCyanContenedorClaro,
    onSecondaryContainer = SolBase02,
    tertiary = SolViolet,
    onTertiary = SolBase3,
    tertiaryContainer = SolVioletContenedorClaro,
    onTertiaryContainer = SolBase02,
    error = SolRed,
    onError = SolBase3,
    errorContainer = SolRedContenedorClaro,
    onErrorContainer = SolBase02,
    background = SolBase3,
    onBackground = SolBase02,
    surface = SolBase3,
    onSurface = SolBase02,
    surfaceVariant = SolBase2,
    onSurfaceVariant = SolBase01,
    outline = SolBase1,
    outlineVariant = SolBase2,
    surfaceContainerLowest = SolBase3,
    surfaceContainerLow = SolBase2,
    surfaceContainer = SolBase2,
    surfaceContainerHigh = SolBase2,
    surfaceContainerHighest = SolBase2
)

private val EsquemaOscuro = darkColorScheme(
    primary = SolBlue,
    onPrimary = SolBase03,
    primaryContainer = SolBlueContenedorOscuro,
    onPrimaryContainer = SolBase2,
    secondary = SolCyan,
    onSecondary = SolBase03,
    secondaryContainer = SolCyanContenedorOscuro,
    onSecondaryContainer = SolBase2,
    tertiary = SolViolet,
    onTertiary = SolBase3,
    tertiaryContainer = SolVioletContenedorOscuro,
    onTertiaryContainer = SolBase2,
    error = SolRed,
    onError = SolBase3,
    errorContainer = SolRedContenedorOscuro,
    onErrorContainer = SolBase2,
    background = SolBase03,
    onBackground = SolBase1,
    surface = SolBase03,
    onSurface = SolBase1,
    surfaceVariant = SolBase02,
    onSurfaceVariant = SolBase0,
    outline = SolBase01,
    outlineVariant = SolBase02,
    surfaceContainerLowest = SolBase03,
    surfaceContainerLow = SolBase02,
    surfaceContainer = SolBase02,
    surfaceContainerHigh = SolBase02,
    surfaceContainerHighest = SolBase02
)

@Composable
fun AgenciaOportunidadesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro
    val coloresSemanticos = if (darkTheme) SemanticosOscuro else SemanticosClaro

    CompositionLocalProvider(LocalColoresSemanticos provides coloresSemanticos) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}