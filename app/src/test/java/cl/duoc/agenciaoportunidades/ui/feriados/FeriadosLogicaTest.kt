package cl.duoc.agenciaoportunidades.ui.feriados

import cl.duoc.agenciaoportunidades.data.externa.FeriadoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeriadosLogicaTest {

    private fun feriado(fecha: String, nombre: String = "Feriado") =
        FeriadoDto(date = fecha, localName = nombre, name = nombre)

    private val lista = listOf(
        feriado("2026-09-18", "Independencia"),
        feriado("2026-12-08", "Inmaculada Concepción"),
        feriado("2026-12-25", "Navidad")
    )

    // ---------- proximoFeriado ----------

    @Test
    fun proximoFeriado_devuelveElPrimeroPosteriorAHoy() {
        assertEquals("Inmaculada Concepción", proximoFeriado(lista, "2026-11-21")?.localName)
    }

    @Test
    fun proximoFeriado_siHoyEsFeriado_loIncluye() {
        assertEquals("2026-12-08", proximoFeriado(lista, "2026-12-08")?.date)
    }

    @Test
    fun proximoFeriado_siYaPasaronTodos_devuelveNulo() {
        assertNull(proximoFeriado(lista, "2026-12-30"))
    }

    @Test
    fun proximoFeriado_conListaVacia_devuelveNulo() {
        assertNull(proximoFeriado(emptyList(), "2026-01-01"))
    }

    @Test
    fun proximoFeriado_conListaDesordenada_igualEncuentraElMasCercano() {
        val desordenada = listOf(lista[2], lista[0], lista[1])
        assertEquals("2026-12-08", proximoFeriado(desordenada, "2026-11-21")?.date)
    }

    // ---------- formatearFechaIso ----------

    @Test
    fun formatearFechaIso_cambiaAlFormatoDiaMesAnio() {
        assertEquals("25-12-2026", formatearFechaIso("2026-12-25"))
    }

    @Test
    fun formatearFechaIso_siElFormatoEsInvalido_dejaElTextoIgual() {
        assertEquals("sin-fecha", formatearFechaIso("sin-fecha"))
        assertEquals("", formatearFechaIso(""))
    }

    // ---------- fechaHoyIso ----------

    @Test
    fun fechaHoyIso_tieneFormatoAnioMesDia() {
        assertTrue(Regex("""\d{4}-\d{2}-\d{2}""").matches(fechaHoyIso()))
    }
}
