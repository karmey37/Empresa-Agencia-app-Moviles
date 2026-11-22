package cl.duoc.agenciaoportunidades.data.externa

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class FeriadosRepositoryTest {

    private lateinit var servidor: MockWebServer
    private lateinit var repository: FeriadosRepository

    @Before
    fun preparar() {
        servidor = MockWebServer()
        servidor.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        repository = FeriadosRepository(retrofit.create(FeriadosApiService::class.java))
    }

    @After
    fun finalizar() {
        servidor.shutdown()
    }

    private fun httpException(codigo: Int) =
        HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

    // ---------- GET /api/v3/PublicHolidays/{anio}/{pais} ----------

    @Test
    fun obtener_http200_devuelveLosFeriadosOrdenadosPorFecha() = runBlocking {
        // El servidor los entrega desordenados a propósito
        val json = """
            [
              {"date":"2026-12-25","localName":"Navidad","name":"Christmas Day","countryCode":"CL","global":true,"types":["Public"]},
              {"date":"2026-01-01","localName":"Año Nuevo","name":"New Year's Day","countryCode":"CL","global":true,"types":["Public"]}
            ]
        """.trimIndent()
        servidor.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(json)
        )

        val resultado = repository.obtener(2026)

        assertTrue(resultado.isSuccess)
        val lista = resultado.getOrThrow()
        assertEquals(2, lista.size)
        assertEquals("Año Nuevo", lista[0].localName)
        assertEquals("2026-12-25", lista[1].date)
        assertEquals("Christmas Day", lista[1].name)
        assertEquals("CL", lista[1].countryCode)
        assertEquals(true, lista[1].global)
    }

    @Test
    fun obtener_armaLaUrlConAnioYPais() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        repository.obtener(2027)

        val peticion = servidor.takeRequest()
        assertEquals("GET", peticion.method)
        assertEquals("/api/v3/PublicHolidays/2027/CL", peticion.path)
    }

    @Test
    fun obtener_conOtroPais_usaEsePaisEnLaUrl() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        repository.obtener(2026, "AR")

        assertEquals("/api/v3/PublicHolidays/2026/AR", servidor.takeRequest().path)
    }

    @Test
    fun obtener_listaVacia_esExitoYNoFalla() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val resultado = repository.obtener(2026)

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrThrow().isEmpty())
    }

    @Test
    fun obtener_campoOpcionalAusente_quedaNulo() = runBlocking {
        val json = """[{"date":"2026-05-01","localName":"Día del Trabajo","name":"Labour Day"}]"""
        servidor.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val feriado = repository.obtener(2026).getOrThrow().first()

        assertNull(feriado.countryCode)
        assertNull(feriado.global)
    }

    @Test
    fun obtener_http404_retornaFalloConHttpException() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(404))

        val resultado = repository.obtener(1800)

        assertTrue(resultado.isFailure)
        val error = resultado.exceptionOrNull()
        assertTrue(error is HttpException)
        assertEquals(404, (error as HttpException).code())
    }

    @Test
    fun obtener_http500_retornaFalloConHttpException() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(500))

        val resultado = repository.obtener(2026)

        assertTrue(resultado.isFailure)
        assertEquals(500, (resultado.exceptionOrNull() as HttpException).code())
    }

    @Test
    fun obtener_sinConexion_retornaFalloConIOException() = runBlocking {
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val resultado = repository.obtener(2026)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is IOException)
    }

    @Test
    fun obtener_jsonMalformado_retornaFalloSinCerrarLaApp() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(200).setBody("esto no es json"))

        val resultado = repository.obtener(2026)

        assertTrue(resultado.isFailure)
    }

    @Test(expected = CancellationException::class)
    fun obtener_siSeCancelaLaCorrutina_serelanzaLaExcepcion() {
        val apiQueSeCancela = object : FeriadosApiService {
            override suspend fun feriados(anio: Int, pais: String): List<FeriadoDto> {
                throw CancellationException("cancelada")
            }
        }

        runBlocking { FeriadosRepository(apiQueSeCancela).obtener(2026) }
    }

    // ---------- mensajes de error para el usuario ----------

    @Test
    fun mensajeDeErrorFeriados_http_incluyeElCodigo() {
        assertEquals(
            "El servicio de feriados respondió con error 503",
            mensajeDeErrorFeriados(httpException(503))
        )
    }

    @Test
    fun mensajeDeErrorFeriados_sinRed_pideRevisarInternet() {
        assertEquals(
            "No se pudo conectar con el servicio de feriados. Revisa tu conexión a internet.",
            mensajeDeErrorFeriados(IOException("sin red"))
        )
    }

    @Test
    fun mensajeDeErrorFeriados_otroError_esUnMensajeGenerico() {
        val mensaje = mensajeDeErrorFeriados(IllegalStateException("algo raro"))

        assertEquals("Ocurrió un error inesperado al leer los feriados", mensaje)
        assertFalse(mensaje.contains("algo raro"))
    }
}
