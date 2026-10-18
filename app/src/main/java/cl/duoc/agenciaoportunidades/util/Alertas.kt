package cl.duoc.agenciaoportunidades.util

import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import java.util.Calendar
import kotlin.math.roundToInt

private const val DIA_MS = 24L * 60 * 60 * 1000

private fun inicioDelDia(millis: Long): Long {
    val calendario = Calendar.getInstance()
    calendario.timeInMillis = millis
    calendario.set(Calendar.HOUR_OF_DAY, 0)
    calendario.set(Calendar.MINUTE, 0)
    calendario.set(Calendar.SECOND, 0)
    calendario.set(Calendar.MILLISECOND, 0)
    return calendario.timeInMillis
}

//dias entre hoy y una fecha (negativo si ya pasó)
fun diasHasta(fecha: Long, ahora: Long): Int {
    val diferencia = inicioDelDia(fecha) - inicioDelDia(ahora)
    return (diferencia.toDouble() / DIA_MS).roundToInt()
}

fun textoPlazo(dias: Int): String = when {
    dias == 0 -> "hoy"
    dias == 1 -> "mañana"
    dias == -1 -> "ayer"
    dias > 1 -> "en $dias días"
    else -> "hace ${-dias} días"
}

//eventos de oportunidades abiertas que ocurren dentro de los próximos días
fun eventosProximos(
    resumenes: List<OportunidadResumen>,
    ahora: Long,
    limiteDias: Int = 30
): List<OportunidadResumen> {
    return resumenes
        .filter { estaAbierta(it.estado) && diasHasta(it.fechaInicio, ahora) in 0..limiteDias }
        .sortedBy { it.fechaInicio }
}