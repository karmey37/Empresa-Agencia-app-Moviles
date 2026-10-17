package cl.duoc.agenciaoportunidades.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Prioridad

// Un color de fondo y el color del texto que se lee bien encima
@Immutable
data class ParDeColor(val fondo: Color, val texto: Color)

// Colores con significado propio de la app (el tema de Material no los trae)
@Immutable
data class ColoresSemanticos(
    val exito: ParDeColor,
    val advertencia: ParDeColor,
    val alerta: ParDeColor,
    val peligro: ParDeColor,
    val info: ParDeColor,
    val progreso: ParDeColor,
    val nuevo: ParDeColor,
    val evaluacion: ParDeColor,
    val neutro: ParDeColor
)

// el texto es oscuro sobre los acentos claros (amarillo, verde, cian, azul)
// y claro sobre los acentos más oscuros (naranja, rojo, magenta, violeta)
private val AcentosComunes = ColoresSemanticos(
    exito = ParDeColor(SolGreen, SolBase03),
    advertencia = ParDeColor(SolYellow, SolBase03),
    alerta = ParDeColor(SolOrange, SolBase3),
    peligro = ParDeColor(SolRed, SolBase3),
    info = ParDeColor(SolBlue, SolBase03),
    progreso = ParDeColor(SolCyan, SolBase03),
    nuevo = ParDeColor(SolViolet, SolBase3),
    evaluacion = ParDeColor(SolMagenta, SolBase3),
    neutro = ParDeColor(SolBase1, SolBase03)
)

val SemanticosClaro = AcentosComunes

val SemanticosOscuro = AcentosComunes.copy(
    neutro = ParDeColor(SolBase01, SolBase3)
)

val LocalColoresSemanticos = staticCompositionLocalOf { SemanticosClaro }

// Uso en una pantalla
val MaterialTheme.coloresApp: ColoresSemanticos
    @Composable
    @ReadOnlyComposable
    get() = LocalColoresSemanticos.current

fun ColoresSemanticos.paraEstado(estado: EstadoOportunidad): ParDeColor = when (estado) {
    EstadoOportunidad.EVENTO_IDENTIFICADO -> nuevo
    EstadoOportunidad.CONTACTO_PENDIENTE -> alerta
    EstadoOportunidad.CONTACTO_REALIZADO -> info
    EstadoOportunidad.PROPUESTA_ENVIADA -> progreso
    EstadoOportunidad.EN_ESPERA_RESPUESTA -> advertencia
    EstadoOportunidad.PARTICIPACION_CONFIRMADA -> exito
    EstadoOportunidad.OPORTUNIDAD_DESCARTADA -> peligro
    EstadoOportunidad.EN_EVALUACION -> evaluacion
    EstadoOportunidad.INFORMACION_INCOMPLETA -> alerta
    EstadoOportunidad.REQUIERE_SEGUIMIENTO -> advertencia
    EstadoOportunidad.ACTIVACION_REALIZADA -> exito
    EstadoOportunidad.ACTIVACION_CANCELADA -> peligro
    EstadoOportunidad.OPORTUNIDAD_CERRADA -> neutro
}

fun ColoresSemanticos.paraPrioridad(prioridad: Prioridad): ParDeColor = when (prioridad) {
    Prioridad.ALTA -> peligro
    Prioridad.MEDIA -> advertencia
    Prioridad.BAJA -> progreso
}