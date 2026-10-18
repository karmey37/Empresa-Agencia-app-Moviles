package cl.duoc.agenciaoportunidades.util

import android.content.Intent
import android.provider.CalendarContract
import cl.duoc.agenciaoportunidades.model.OportunidadDetalle
import cl.duoc.agenciaoportunidades.model.SeguimientoDetalle
import java.util.Calendar

private const val MINUTOS_SEGUIMIENTO = 30
private const val HORA_SEGUIMIENTO = 9

private fun inicioDeDia(millis: Long): Long {
    val calendario = Calendar.getInstance()
    calendario.timeInMillis = millis
    calendario.set(Calendar.HOUR_OF_DAY, 0)
    calendario.set(Calendar.MINUTE, 0)
    calendario.set(Calendar.SECOND, 0)
    calendario.set(Calendar.MILLISECOND, 0)
    return calendario.timeInMillis
}

private fun conHora(dia: Long, minutosDelDia: Int): Long {
    val calendario = Calendar.getInstance()
    calendario.timeInMillis = inicioDeDia(dia)
    calendario.set(Calendar.HOUR_OF_DAY, minutosDelDia / 60)
    calendario.set(Calendar.MINUTE, minutosDelDia % 60)
    return calendario.timeInMillis
}

private fun minutosDelDia(texto: String): Int? {
    val partes = texto.trim().split(":")
    if (partes.size != 2) return null
    val hora = partes[0].toIntOrNull() ?: return null
    val minuto = partes[1].toIntOrNull() ?: return null
    return hora * 60 + minuto
}

// Convierte "10:00 - 16:00" en (inicio, término) expresados en minutos del día
fun parsearHorario(horario: String): Pair<Int, Int>? {
    val partes = horario.trim().split(" - ")
    if (partes.size != 2) return null
    val inicio = minutosDelDia(partes[0]) ?: return null
    val fin = minutosDelDia(partes[1]) ?: return null
    return Pair(inicio, fin)
}

private fun intentInsertarEvento(
    titulo: String,
    descripcion: String,
    ubicacion: String,
    inicio: Long,
    fin: Long,
    todoElDia: Boolean
): Intent {
    return Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, titulo)
        putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
        putExtra(CalendarContract.Events.EVENT_LOCATION, ubicacion)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fin)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, todoElDia)
    }
}

// Evento de la oportunidad: desde el inicio del primer día hasta el término del último,
// usando el horario registrado (si no tiene un horario válido, queda como día completo)
fun intentCalendarioEvento(detalle: OportunidadDetalle): Intent {
    val horas = parsearHorario(detalle.horario)

    val inicio: Long
    val fin: Long
    if (horas != null) {
        inicio = conHora(detalle.fechaInicio, horas.first)
        fin = conHora(detalle.fechaTermino, horas.second)
    } else {
        inicio = inicioDeDia(detalle.fechaInicio)
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = inicioDeDia(detalle.fechaTermino)
        calendario.add(Calendar.DAY_OF_YEAR, 1)
        fin = calendario.timeInMillis
    }

    return intentInsertarEvento(
        titulo = detalle.eventoNombre,
        descripcion = "Oportunidad: ${detalle.nombre}\nOrganización: ${detalle.organizacion}",
        ubicacion = "${detalle.ubicacion}, ${detalle.comuna}",
        inicio = inicio,
        fin = fin,
        todoElDia = horas == null
    )
}

// Seguimiento programado: queda a las 09:00 del día indicado, con 30 minutos de duración
// (el usuario puede ajustar la hora en la app de calendario antes de guardar)
fun intentCalendarioSeguimiento(seguimiento: SeguimientoDetalle, fecha: Long): Intent {
    val inicio = conHora(fecha, HORA_SEGUIMIENTO * 60)
    val fin = conHora(fecha, HORA_SEGUIMIENTO * 60 + MINUTOS_SEGUIMIENTO)
    val accion = seguimiento.proximaAccion.ifBlank { seguimiento.tipoAccion.etiqueta() }

    return intentInsertarEvento(
        titulo = "Seguimiento: $accion",
        descripcion = "Oportunidad: ${seguimiento.oportunidadNombre}",
        ubicacion = "",
        inicio = inicio,
        fin = fin,
        todoElDia = false
    )
}