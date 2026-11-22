package cl.duoc.agenciaoportunidades.ui.feriados

import cl.duoc.agenciaoportunidades.data.externa.FeriadoDto
import java.util.Calendar
import java.util.Locale

// Fecha de hoy en formato ISO (yyyy-MM-dd). Se usa Calendar y no java.time
// porque la app soporta desde Android 7.0 (minSdk 24).
fun fechaHoyIso(): String {
    val c = Calendar.getInstance()
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
}

// Primer feriado que cae hoy o después de hoy. Las fechas ISO se comparan como texto.
fun proximoFeriado(feriados: List<FeriadoDto>, hoy: String): FeriadoDto? =
    feriados.filter { it.date >= hoy }.minByOrNull { it.date }

// "2026-12-25" -> "25-12-2026". Si el formato no es el esperado, se deja igual.
fun formatearFechaIso(iso: String): String {
    val partes = iso.split("-")
    return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else iso
}
