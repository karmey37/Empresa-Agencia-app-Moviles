package cl.duoc.agenciaoportunidades.util

import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.PerfilUsuario
import cl.duoc.agenciaoportunidades.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasNegocioTest {

    private val supervisor = Usuario(
        id = 10,
        nombre = "Supervisor de prueba",
        correo = "supervisor@agencia.cl",
        password = "123456",
        perfil = PerfilUsuario.SUPERVISOR
    )

    private val ejecutivo = Usuario(
        id = 1,
        nombre = "Ejecutivo de prueba",
        correo = "ejecutivo@agencia.cl",
        password = "123456",
        perfil = PerfilUsuario.EJECUTIVO
    )

    // ---------- esSupervisor ----------

    @Test
    fun esSupervisor_conPerfilSupervisor_retornaTrue() {
        assertTrue(esSupervisor(supervisor))
    }

    @Test
    fun esSupervisor_conPerfilEjecutivo_retornaFalse() {
        assertFalse(esSupervisor(ejecutivo))
    }

    // ---------- puedeVerOportunidad ----------

    @Test
    fun puedeVerOportunidad_supervisorVeTodo_retornaTrue() {
        assertTrue(puedeVerOportunidad(supervisor, 99))
    }

    @Test
    fun puedeVerOportunidad_ejecutivoConOportunidadPropia_retornaTrue() {
        assertTrue(puedeVerOportunidad(ejecutivo, 1))
    }

    @Test
    fun puedeVerOportunidad_ejecutivoConOportunidadAjena_retornaFalse() {
        assertFalse(puedeVerOportunidad(ejecutivo, 2))
    }

    @Test
    fun puedeVerOportunidad_ejecutivoSinResponsable_retornaFalse() {
        assertFalse(puedeVerOportunidad(ejecutivo, null))
    }

    // ---------- puedeAsignarResponsable ----------

    @Test
    fun puedeAsignarResponsable_supervisor_retornaTrue() {
        assertTrue(puedeAsignarResponsable(supervisor))
    }

    @Test
    fun puedeAsignarResponsable_ejecutivo_retornaFalse() {
        assertFalse(puedeAsignarResponsable(ejecutivo))
    }

    // ---------- estadosPermitidos ----------

    @Test
    fun estadosPermitidos_ejecutivoDesdeEventoIdentificado_noIncluyeDescartar() {
        val resultado = estadosPermitidos(ejecutivo, EstadoOportunidad.EVENTO_IDENTIFICADO)

        assertEquals(
            listOf(
                EstadoOportunidad.CONTACTO_PENDIENTE,
                EstadoOportunidad.EN_EVALUACION,
                EstadoOportunidad.INFORMACION_INCOMPLETA
            ),
            resultado
        )
    }

    @Test
    fun estadosPermitidos_ejecutivoDesdePropuestaEnviada_noPuedeConfirmarNiDescartar() {
        val resultado = estadosPermitidos(ejecutivo, EstadoOportunidad.PROPUESTA_ENVIADA)

        assertEquals(
            listOf(
                EstadoOportunidad.EN_ESPERA_RESPUESTA,
                EstadoOportunidad.REQUIERE_SEGUIMIENTO
            ),
            resultado
        )
    }

    @Test
    fun estadosPermitidos_supervisorDesdePropuestaEnviada_veTodasLasTransiciones() {
        val resultado = estadosPermitidos(supervisor, EstadoOportunidad.PROPUESTA_ENVIADA)

        assertEquals(
            listOf(
                EstadoOportunidad.EN_ESPERA_RESPUESTA,
                EstadoOportunidad.REQUIERE_SEGUIMIENTO,
                EstadoOportunidad.PARTICIPACION_CONFIRMADA,
                EstadoOportunidad.OPORTUNIDAD_DESCARTADA
            ),
            resultado
        )
    }

    @Test
    fun estadosPermitidos_ejecutivoDesdeEstadoFinal_retornaListaVacia() {
        val resultado = estadosPermitidos(ejecutivo, EstadoOportunidad.OPORTUNIDAD_DESCARTADA)

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun estadosPermitidos_ejecutivoDesdeActivacionCancelada_retornaListaVacia() {
        val resultado = estadosPermitidos(ejecutivo, EstadoOportunidad.ACTIVACION_CANCELADA)

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun estadosPermitidos_supervisorReabreOportunidadDescartada() {
        val resultado = estadosPermitidos(supervisor, EstadoOportunidad.OPORTUNIDAD_DESCARTADA)

        assertEquals(listOf(EstadoOportunidad.EN_EVALUACION), resultado)
    }

    @Test
    fun estadosPermitidos_supervisorDesdeOportunidadCerrada_retornaListaVacia() {
        val resultado = estadosPermitidos(supervisor, EstadoOportunidad.OPORTUNIDAD_CERRADA)

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun estadosPermitidos_ejecutivoDesdeParticipacionConfirmada_soloPuedeRealizar() {
        val resultado = estadosPermitidos(ejecutivo, EstadoOportunidad.PARTICIPACION_CONFIRMADA)

        assertEquals(listOf(EstadoOportunidad.ACTIVACION_REALIZADA), resultado)
    }

    @Test
    fun estadosPermitidos_ejecutivoDesdeActivacionRealizada_noPuedeCerrar() {
        val resultado = estadosPermitidos(ejecutivo, EstadoOportunidad.ACTIVACION_REALIZADA)

        assertTrue(resultado.isEmpty())
    }

    // ---------- estaAbierta ----------

    @Test
    fun estaAbierta_enEvaluacion_retornaTrue() {
        assertTrue(estaAbierta(EstadoOportunidad.EN_EVALUACION))
    }

    @Test
    fun estaAbierta_participacionConfirmada_retornaTrue() {
        assertTrue(estaAbierta(EstadoOportunidad.PARTICIPACION_CONFIRMADA))
    }

    @Test
    fun estaAbierta_descartada_retornaFalse() {
        assertFalse(estaAbierta(EstadoOportunidad.OPORTUNIDAD_DESCARTADA))
    }

    @Test
    fun estaAbierta_activacionRealizada_retornaFalse() {
        assertFalse(estaAbierta(EstadoOportunidad.ACTIVACION_REALIZADA))
    }

    @Test
    fun estaAbierta_cerrada_retornaFalse() {
        assertFalse(estaAbierta(EstadoOportunidad.OPORTUNIDAD_CERRADA))
    }

    // ---------- puedeEditarOportunidad ----------

    @Test
    fun puedeEditarOportunidad_supervisorEditaSiempre_retornaTrue() {
        assertTrue(puedeEditarOportunidad(supervisor, EstadoOportunidad.OPORTUNIDAD_CERRADA))
    }

    @Test
    fun puedeEditarOportunidad_ejecutivoConOportunidadAbierta_retornaTrue() {
        assertTrue(puedeEditarOportunidad(ejecutivo, EstadoOportunidad.EN_EVALUACION))
    }

    @Test
    fun puedeEditarOportunidad_ejecutivoConOportunidadCerrada_retornaFalse() {
        assertFalse(puedeEditarOportunidad(ejecutivo, EstadoOportunidad.OPORTUNIDAD_CERRADA))
    }
}