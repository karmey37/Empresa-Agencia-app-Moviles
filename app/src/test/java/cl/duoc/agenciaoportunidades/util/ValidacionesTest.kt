package cl.duoc.agenciaoportunidades.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.Assert.assertNotNull

class ValidacionesTest {

    // ---------- validarCorreo ----------

    @Test
    fun validarCorreo_vacio_retornaError() {
        // ARRANGE
        val correo = ""
        // ACT
        val resultado = validarCorreo(correo)
        // ASSERT
        assertEquals("El correo es obligatorio", resultado)
    }

    @Test
    fun validarCorreo_soloEspacios_retornaError() {
        val resultado = validarCorreo("   ")

        assertEquals("El correo es obligatorio", resultado)
    }

    @Test
    fun validarCorreo_sinArroba_retornaError() {
        val resultado = validarCorreo("ejecutivo1agencia.cl")

        assertEquals("Ingresa un correo válido (ej: nombre@dominio.cl)", resultado)
    }

    @Test
    fun validarCorreo_correcto_retornaNull() {
        val resultado = validarCorreo("ejecutivo1@agencia.cl")

        assertNull(resultado)
    }

    @Test
    fun validarCorreo_conEspaciosAlrededor_seAcepta() {
        val resultado = validarCorreo("  ejecutivo1@agencia.cl  ")

        assertNull(resultado)
    }

    // ---------- validarPassword ----------

    @Test
    fun validarPassword_vacia_retornaError() {
        val resultado = validarPassword("")

        assertEquals("La contraseña es obligatoria", resultado)
    }

    @Test
    fun validarPassword_cincoCaracteres_retornaError() {
        val resultado = validarPassword("12345")

        assertEquals("La contraseña debe tener al menos 6 caracteres", resultado)
    }

    @Test
    fun validarPassword_seisCaracteres_retornaNull() {
        val resultado = validarPassword("123456")

        assertNull(resultado)
    }

    // ---------- validarTextoObligatorio ----------

    @Test
    fun validarTextoObligatorio_vacio_retornaError() {
        val resultado = validarTextoObligatorio("", "El nombre")

        assertEquals("El nombre es obligatorio", resultado)
    }

    @Test
    fun validarTextoObligatorio_muyCorto_retornaError() {
        val resultado = validarTextoObligatorio("ab", "El nombre")

        assertEquals("El nombre debe tener al menos 3 caracteres", resultado)
    }

    @Test
    fun validarTextoObligatorio_muyLargo_retornaError() {
        val resultado = validarTextoObligatorio("a".repeat(101), "El nombre")

        assertEquals("El nombre no puede superar los 100 caracteres", resultado)
    }

    @Test
    fun validarTextoObligatorio_enElLimiteMaximo_retornaNull() {
        val resultado = validarTextoObligatorio("a".repeat(100), "El nombre")

        assertNull(resultado)
    }

    @Test
    fun validarTextoObligatorio_correcto_retornaNull() {
        val resultado = validarTextoObligatorio("Ana", "El nombre")

        assertNull(resultado)
    }
    // ---------- validarTelefono ----------

    @Test
    fun validarTelefono_vacio_retornaError() {
        val resultado = validarTelefono("")

        assertEquals("El teléfono es obligatorio", resultado)
    }

    @Test
    fun validarTelefono_pocosDigitos_retornaError() {
        val resultado = validarTelefono("12345")

        assertEquals("Debe tener 9 dígitos (ej: 912345678)", resultado)
    }

    @Test
    fun validarTelefono_nueveDigitos_retornaNull() {
        val resultado = validarTelefono("912345678")

        assertNull(resultado)
    }

    @Test
    fun validarTelefono_conPrefijoChile_retornaNull() {
        val resultado = validarTelefono("+56912345678")

        assertNull(resultado)
    }

    @Test
    fun validarTelefono_conEspacios_retornaNull() {
        val resultado = validarTelefono("9123 45678")

        assertNull(resultado)
    }

    // ---------- parsearFecha ----------

    @Test
    fun parsearFecha_fechaValida_retornaMilisegundos() {
        val resultado = parsearFecha("31/12/2026")

        assertNotNull(resultado)
    }

    @Test
    fun parsearFecha_diaInexistente_retornaNull() {
        val resultado = parsearFecha("31/02/2026")

        assertNull(resultado)
    }

    @Test
    fun parsearFecha_formatoIncorrecto_retornaNull() {
        val resultado = parsearFecha("2026-12-31")

        assertNull(resultado)
    }

    // ---------- validarFecha ----------

    @Test
    fun validarFecha_vacia_retornaError() {
        val resultado = validarFecha("")

        assertEquals("La fecha es obligatoria", resultado)
    }

    @Test
    fun validarFecha_conCampoPersonalizado_usaElNombreDelCampo() {
        val resultado = validarFecha("", "La fecha de inicio")

        assertEquals("La fecha de inicio es obligatoria", resultado)
    }

    @Test
    fun validarFecha_invalida_retornaError() {
        val resultado = validarFecha("31/02/2026")

        assertEquals("La fecha no es válida (formato dd/mm/aaaa)", resultado)
    }

    @Test
    fun validarFecha_correcta_retornaNull() {
        val resultado = validarFecha("15/03/2026")

        assertNull(resultado)
    }

    // ---------- validarRangoFechas ----------

    @Test
    fun validarRangoFechas_terminoAnteriorAlInicio_retornaError() {
        val resultado = validarRangoFechas(2000L, 1000L)

        assertEquals("La fecha de término no puede ser anterior a la de inicio", resultado)
    }

    @Test
    fun validarRangoFechas_terminoPosteriorAlInicio_retornaNull() {
        val resultado = validarRangoFechas(1000L, 2000L)

        assertNull(resultado)
    }

    @Test
    fun validarRangoFechas_mismaFecha_retornaNull() {
        val resultado = validarRangoFechas(1000L, 1000L)

        assertNull(resultado)
    }

    @Test
    fun validarRangoFechas_sinInicio_retornaNull() {
        val resultado = validarRangoFechas(null, 1000L)

        assertNull(resultado)
    }

    // ---------- validarNumeroEntero ----------

    @Test
    fun validarNumeroEntero_vacio_retornaError() {
        val resultado = validarNumeroEntero("", "El monto")

        assertEquals("El monto es obligatorio", resultado)
    }

    @Test
    fun validarNumeroEntero_noNumerico_retornaError() {
        val resultado = validarNumeroEntero("abc", "El monto")

        assertEquals("El monto debe ser un número entero", resultado)
    }

    @Test
    fun validarNumeroEntero_negativoConMinimoCero_retornaError() {
        val resultado = validarNumeroEntero("-1", "El monto")

        assertEquals("El monto no puede ser negativo", resultado)
    }

    @Test
    fun validarNumeroEntero_menorQueElMinimo_retornaError() {
        val resultado = validarNumeroEntero("3", "El monto", 5)

        assertEquals("El monto no puede ser menor que 5", resultado)
    }

    @Test
    fun validarNumeroEntero_enElLimiteInferior_retornaNull() {
        val resultado = validarNumeroEntero("0", "El monto")

        assertNull(resultado)
    }

    @Test
    fun validarNumeroEntero_valido_retornaNull() {
        val resultado = validarNumeroEntero("150000", "El monto")

        assertNull(resultado)
    }

    // ---------- validarHorario ----------

    @Test
    fun validarHorario_vacio_retornaError() {
        val resultado = validarHorario("")

        assertEquals("El horario es obligatorio", resultado)
    }

    @Test
    fun validarHorario_formatoIncorrecto_retornaError() {
        val resultado = validarHorario("10-16")

        assertEquals("Usa el formato HH:mm - HH:mm (ej: 10:00 - 16:00)", resultado)
    }

    @Test
    fun validarHorario_terminoAnteriorAlInicio_retornaError() {
        val resultado = validarHorario("16:00 - 10:00")

        assertEquals("La hora de término debe ser posterior a la de inicio", resultado)
    }

    @Test
    fun validarHorario_inicioIgualAlTermino_retornaError() {
        val resultado = validarHorario("10:00 - 10:00")

        assertEquals("La hora de término debe ser posterior a la de inicio", resultado)
    }

    @Test
    fun validarHorario_correcto_retornaNull() {
        val resultado = validarHorario("10:00 - 16:00")

        assertNull(resultado)
    }

    // ---------- validarSitioWebOpcional ----------

    @Test
    fun validarSitioWebOpcional_vacio_retornaNull() {
        val resultado = validarSitioWebOpcional("")

        assertNull(resultado)
    }

    @Test
    fun validarSitioWebOpcional_sinProtocolo_retornaError() {
        val resultado = validarSitioWebOpcional("ejemplo")

        assertEquals("Usa una dirección válida (ej: https://www.ejemplo.cl)", resultado)
    }

    @Test
    fun validarSitioWebOpcional_https_retornaNull() {
        val resultado = validarSitioWebOpcional("https://www.ejemplo.cl")

        assertNull(resultado)
    }

    @Test
    fun validarSitioWebOpcional_conRuta_retornaNull() {
        val resultado = validarSitioWebOpcional("http://ejemplo.cl/contacto")

        assertNull(resultado)
    }
}