package cl.duoc.agenciaoportunidades.util

import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.TipoEvento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AlertasTest {

    // Construye una fecha a las 'hora' horas del día indicado (mes de 1 a 12)
    private fun fecha(anio: Int, mes: Int, dia: Int, hora: Int = 12): Long {
        val calendario = Calendar.getInstance()
        calendario.set(anio, mes - 1, dia, hora, 0, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        return calendario.timeInMillis
    }

    private fun resumen(id: Int, estado: EstadoOportunidad, fechaInicio: Long) =
        OportunidadResumen(
            id = id,
            nombre = "Oportunidad $id",
            estado = estado,
            prioridad = Prioridad.MEDIA,
            tipo = TipoEvento.OTRA,
            fechaInicio = fechaInicio,
            comuna = "Santiago",
            region = "Metropolitana de Santiago",
            organizacion = "Organización de prueba",
            responsableId = null,
            responsable = null,
            fechaActualizacion = fechaInicio
        )

    // ---------- diasHasta ----------

    @Test
    fun diasHasta_mismoDiaConDistintaHora_retornaCero() {
        val ahora = fecha(2026, 11, 20, 8)
        val evento = fecha(2026, 11, 20, 22)

        assertEquals(0, diasHasta(evento, ahora))
    }

    @Test
    fun diasHasta_diaSiguiente_retornaUno() {
        val ahora = fecha(2026, 11, 20)
        val evento = fecha(2026, 11, 21)

        assertEquals(1, diasHasta(evento, ahora))
    }

    @Test
    fun diasHasta_diaAnterior_retornaMenosUno() {
        val ahora = fecha(2026, 11, 20)
        val evento = fecha(2026, 11, 19)

        assertEquals(-1, diasHasta(evento, ahora))
    }

    @Test
    fun diasHasta_diezDiasDespues_retornaDiez() {
        val ahora = fecha(2026, 11, 20)
        val evento = fecha(2026, 11, 30)

        assertEquals(10, diasHasta(evento, ahora))
    }

    // ---------- textoPlazo ----------

    @Test
    fun textoPlazo_cero_retornaHoy() {
        assertEquals("hoy", textoPlazo(0))
    }

    @Test
    fun textoPlazo_uno_retornaManana() {
        assertEquals("mañana", textoPlazo(1))
    }

    @Test
    fun textoPlazo_menosUno_retornaAyer() {
        assertEquals("ayer", textoPlazo(-1))
    }

    @Test
    fun textoPlazo_cincoDias_retornaEnCincoDias() {
        assertEquals("en 5 días", textoPlazo(5))
    }

    @Test
    fun textoPlazo_menosCinco_retornaHaceCincoDias() {
        assertEquals("hace 5 días", textoPlazo(-5))
    }

    // ---------- eventosProximos ----------

    private val ahora = fecha(2026, 11, 20)

    private val hoy = resumen(1, EstadoOportunidad.EN_EVALUACION, fecha(2026, 11, 20, 18))
    private val enCincoDias = resumen(2, EstadoOportunidad.PROPUESTA_ENVIADA, fecha(2026, 11, 25))
    private val muyLejano = resumen(3, EstadoOportunidad.EN_EVALUACION, fecha(2027, 1, 1))
    private val cerrada = resumen(4, EstadoOportunidad.OPORTUNIDAD_CERRADA, fecha(2026, 11, 23))
    private val yaPaso = resumen(5, EstadoOportunidad.EN_EVALUACION, fecha(2026, 11, 19))

    @Test
    fun eventosProximos_filtraAbiertasDentroDelLimiteYOrdenaPorFecha() {
        val lista = listOf(enCincoDias, muyLejano, cerrada, yaPaso, hoy)

        val resultado = eventosProximos(lista, ahora)

        assertEquals(listOf(1, 2), resultado.map { it.id })
    }

    @Test
    fun eventosProximos_conLimiteMayor_incluyeEventosMasLejanos() {
        val lista = listOf(enCincoDias, muyLejano, hoy)

        val resultado = eventosProximos(lista, ahora, limiteDias = 60)

        assertEquals(listOf(1, 2, 3), resultado.map { it.id })
    }

    @Test
    fun eventosProximos_listaVacia_retornaListaVacia() {
        val resultado = eventosProximos(emptyList(), ahora)

        assertTrue(resultado.isEmpty())
    }
}