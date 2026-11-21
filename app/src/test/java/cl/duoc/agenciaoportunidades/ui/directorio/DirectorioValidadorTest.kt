package cl.duoc.agenciaoportunidades.ui.directorio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectorioValidadorTest {

    private fun estadoValido() = DirectorioUiState(
        nombre = "Centro Test",
        rubro = "Comercio",
        comuna = "Santiago"
    )

    @Test
    fun validarDirectorio_datosCorrectos_noHayErrores() {
        val errores = validarDirectorio(estadoValido())

        assertTrue(errores.isEmpty())
    }

    @Test
    fun validarDirectorio_todoVacio_fallanLosTresCamposObligatorios() {
        val errores = validarDirectorio(DirectorioUiState())

        assertEquals(
            setOf(CampoDirectorio.NOMBRE, CampoDirectorio.RUBRO, CampoDirectorio.COMUNA),
            errores.keys
        )
        assertEquals("El nombre es obligatorio", errores[CampoDirectorio.NOMBRE])
        assertEquals("El rubro es obligatorio", errores[CampoDirectorio.RUBRO])
        assertEquals("La comuna es obligatorio", errores[CampoDirectorio.COMUNA])
    }

    @Test
    fun validarDirectorio_nombreMuyCorto_retornaErrorDeNombre() {
        val errores = validarDirectorio(estadoValido().copy(nombre = "ab"))

        assertEquals(
            "El nombre debe tener al menos 3 caracteres",
            errores[CampoDirectorio.NOMBRE]
        )
    }

    @Test
    fun validarDirectorio_sitioWebVacio_esValido() {
        val errores = validarDirectorio(estadoValido().copy(sitioWeb = ""))

        assertFalse(errores.containsKey(CampoDirectorio.SITIO_WEB))
    }

    @Test
    fun validarDirectorio_sitioWebInvalido_retornaErrorDeSitioWeb() {
        val errores = validarDirectorio(estadoValido().copy(sitioWeb = "ejemplo"))

        assertEquals(
            "Usa una dirección válida (ej: https://www.ejemplo.cl)",
            errores[CampoDirectorio.SITIO_WEB]
        )
    }

    @Test
    fun validarDirectorio_sitioWebCorrecto_noHayErrores() {
        val errores = validarDirectorio(
            estadoValido().copy(sitioWeb = "https://www.ejemplo.cl")
        )

        assertTrue(errores.isEmpty())
    }
}