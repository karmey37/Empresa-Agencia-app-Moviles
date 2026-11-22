package cl.duoc.agenciaoportunidades.ui.feriados

import cl.duoc.agenciaoportunidades.data.externa.FeriadoDto
import cl.duoc.agenciaoportunidades.data.externa.FeriadosApiFake
import cl.duoc.agenciaoportunidades.data.externa.FeriadosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FeriadosViewModelTest {

    private lateinit var api: FeriadosApiFake
    private val dispatcher = StandardTestDispatcher()

    private fun feriado(fecha: String, nombre: String) =
        FeriadoDto(date = fecha, localName = nombre, name = nombre, countryCode = "CL", global = true)

    @Before
    fun preparar() {
        // viewModelScope usa el hilo principal de Android, que no existe en una prueba JVM
        Dispatchers.setMain(dispatcher)
        api = FeriadosApiFake()
        api.respuestas[2026] = listOf(
            feriado("2026-12-25", "Navidad"),
            feriado("2026-09-18", "Independencia"),
            feriado("2026-12-08", "Inmaculada Concepción")
        )
        api.respuestas[2027] = listOf(feriado("2027-01-01", "Año Nuevo"))
    }

    @After
    fun finalizar() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(hoy: String = "2026-11-21") =
        FeriadosViewModel(FeriadosRepository(api), hoy)

    // Crea el ViewModel y deja terminar la carga inicial
    private fun TestScope.viewModelListo(hoy: String = "2026-11-21"): FeriadosViewModel {
        val viewModel = crearViewModel(hoy)
        advanceUntilIdle()
        return viewModel
    }

    private fun httpException(codigo: Int) =
        HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

    // ---------- carga ----------

    @Test
    fun estadoInicial_estaCargandoEnElAnioActual() = runTest {
        val viewModel = crearViewModel()

        val ui = viewModel.uiState.value
        assertTrue(ui.cargando)
        assertEquals(2026, ui.anio)
        assertTrue(ui.feriados.isEmpty())
    }

    @Test
    fun cargaInicial_consultaElAnioActualParaChile() = runTest {
        val viewModel = viewModelListo()

        assertEquals(listOf(2026 to "CL"), api.consultas)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun cargaInicial_muestraLosFeriadosOrdenadosPorFecha() = runTest {
        val viewModel = viewModelListo()

        assertEquals(
            listOf("2026-09-18", "2026-12-08", "2026-12-25"),
            viewModel.uiState.value.feriados.map { it.date }
        )
    }

    // ---------- próximo feriado ----------

    @Test
    fun proximo_esElPrimeroDespuesDeHoy() = runTest {
        val viewModel = viewModelListo(hoy = "2026-11-21")

        assertEquals("Inmaculada Concepción", viewModel.uiState.value.proximo?.localName)
    }

    @Test
    fun proximo_esNuloSiYaPasaronTodosLosFeriadosDelAnio() = runTest {
        val viewModel = viewModelListo(hoy = "2026-12-30")

        assertNull(viewModel.uiState.value.proximo)
    }

    @Test
    fun proximo_esNuloAlMirarOtroAnio() = runTest {
        val viewModel = viewModelListo()

        viewModel.cambiarAnio(1)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.proximo)
    }

    // ---------- errores ----------

    @Test
    fun sinConexion_muestraMensajeYDejaLaListaVacia() = runTest {
        api.error = IOException("sin red")

        val viewModel = viewModelListo()

        val ui = viewModel.uiState.value
        assertFalse(ui.cargando)
        assertTrue(ui.feriados.isEmpty())
        assertEquals(
            "No se pudo conectar con el servicio de feriados. Revisa tu conexión a internet.",
            ui.mensaje
        )
    }

    @Test
    fun errorHttp_muestraElCodigoDelServidor() = runTest {
        api.error = httpException(500)

        val viewModel = viewModelListo()

        assertEquals(
            "El servicio de feriados respondió con error 500",
            viewModel.uiState.value.mensaje
        )
    }

    @Test
    fun consumirMensaje_limpiaElMensaje() = runTest {
        api.error = IOException("sin red")
        val viewModel = viewModelListo()

        viewModel.consumirMensaje()

        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun recargar_despuesDeUnError_cargaLosFeriados() = runTest {
        api.error = IOException("sin red")
        val viewModel = viewModelListo()
        assertTrue(viewModel.uiState.value.feriados.isEmpty())

        api.error = null
        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.feriados.size)
        assertEquals(2, api.consultas.size)
    }

    // ---------- cambio de año ----------

    @Test
    fun cambiarAnio_consultaElAnioNuevo() = runTest {
        val viewModel = viewModelListo()

        viewModel.cambiarAnio(1)
        advanceUntilIdle()

        val ui = viewModel.uiState.value
        assertEquals(2027, ui.anio)
        assertEquals(listOf("Año Nuevo"), ui.feriados.map { it.localName })
        assertEquals(2027 to "CL", api.consultas.last())
    }

    @Test
    fun cambiarAnio_haciaAtras_restaUnAnio() = runTest {
        val viewModel = viewModelListo()

        viewModel.cambiarAnio(-1)
        advanceUntilIdle()

        assertEquals(2025, viewModel.uiState.value.anio)
    }

    @Test
    fun cambiarAnioRapido_soloSeConsultaElUltimoAnio() = runTest {
        val viewModel = crearViewModel()

        // Dos cambios seguidos antes de que termine cualquier carga
        viewModel.cambiarAnio(1)
        viewModel.cambiarAnio(1)
        advanceUntilIdle()

        assertEquals(2028, viewModel.uiState.value.anio)
        assertEquals(listOf(2028 to "CL"), api.consultas)
    }
}
