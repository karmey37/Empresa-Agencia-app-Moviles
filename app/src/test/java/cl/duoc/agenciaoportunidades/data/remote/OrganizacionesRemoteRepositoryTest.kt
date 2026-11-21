package cl.duoc.agenciaoportunidades.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class OrganizacionesRemoteRepositoryTest {

    private lateinit var servidor: MockWebServer
    private lateinit var repository: OrganizacionesRemoteRepository

    @Before
    fun preparar() {
        servidor = MockWebServer()
        servidor.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        repository = OrganizacionesRemoteRepository(
            retrofit.create(AgenciaApiService::class.java)
        )
    }

    @After
    fun finalizar() {
        servidor.shutdown()
    }

    private fun httpException(codigo: Int) =
        HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

    private val organizacionNueva = OrganizacionDto(
        nombre = "Centro Test",
        tipo = "EMPRESA",
        rubro = "Comercio",
        comuna = "Santiago"
    )

    // ---------- GET /organizaciones ----------

    @Test
    fun listar_http200_devuelveLaLista() = runBlocking {
        val json = """
            [
              {"id":1,"nombre":"Retail Andino S.A.","tipo":"EMPRESA","rubro":"Retail","comuna":"Santiago"},
              {"id":2,"nombre":"Municipalidad de Puente Alto","tipo":"MUNICIPALIDAD","rubro":"Gobierno local","comuna":"Puente Alto"}
            ]
        """.trimIndent()
        servidor.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(json)
        )

        val resultado = repository.listar()

        assertTrue(resultado.isSuccess)
        val lista = resultado.getOrThrow()
        assertEquals(2, lista.size)
        assertEquals("Retail Andino S.A.", lista[0].nombre)
        assertEquals("MUNICIPALIDAD", lista[1].tipo)

        val peticion = servidor.takeRequest()
        assertEquals("GET", peticion.method)
        assertEquals("/organizaciones", peticion.path)
    }

    @Test
    fun listar_http500_retornaFalloConHttpException() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(500))

        val resultado = repository.listar()

        assertTrue(resultado.isFailure)
        val error = resultado.exceptionOrNull()
        assertTrue(error is HttpException)
        assertEquals(500, (error as HttpException).code())
    }

    // ---------- POST /organizaciones ----------

    @Test
    fun crear_http201_enviaElCuerpoYDevuelveLaOrganizacionCreada() = runBlocking {
        val json = """
            {"id":4,"nombre":"Centro Test","tipo":"EMPRESA","rubro":"Comercio","comuna":"Santiago"}
        """.trimIndent()
        servidor.enqueue(
            MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody(json)
        )

        val resultado = repository.crear(organizacionNueva)

        assertTrue(resultado.isSuccess)
        assertEquals(4L, resultado.getOrThrow().id)

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/organizaciones", peticion.path)
        assertTrue(peticion.body.readUtf8().contains("\"nombre\":\"Centro Test\""))
    }

    // ---------- PUT /organizaciones/{id} ----------

    @Test
    fun actualizar_http200_usaElIdEnLaRuta() = runBlocking {
        val json = """
            {"id":7,"nombre":"Centro Test","tipo":"EMPRESA","rubro":"Comercio","comuna":"Santiago"}
        """.trimIndent()
        servidor.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(json)
        )

        val resultado = repository.actualizar(7L, organizacionNueva)

        assertTrue(resultado.isSuccess)
        assertEquals(7L, resultado.getOrThrow().id)

        val peticion = servidor.takeRequest()
        assertEquals("PUT", peticion.method)
        assertEquals("/organizaciones/7", peticion.path)
    }

    // ---------- DELETE /organizaciones/{id} ----------

    @Test
    fun borrar_http204_retornaExito() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(204))

        val resultado = repository.borrar(7L)

        assertTrue(resultado.isSuccess)

        val peticion = servidor.takeRequest()
        assertEquals("DELETE", peticion.method)
        assertEquals("/organizaciones/7", peticion.path)
    }

    @Test
    fun borrar_http404_retornaFalloConHttpException() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(404))

        val resultado = repository.borrar(99L)

        assertTrue(resultado.isFailure)
        val error = resultado.exceptionOrNull()
        assertTrue(error is HttpException)
        assertEquals(404, (error as HttpException).code())
    }

    // ---------- errores de red y cancelación (con la API falsa) ----------

    @Test
    fun listar_sinConexion_retornaFalloConIOException() = runBlocking {
        val api = ApiFake()
        api.errorAlListar = IOException("sin red")

        val resultado = OrganizacionesRemoteRepository(api).listar()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is IOException)
    }

    @Test
    fun listar_siSeCancelaLaCorrutina_relanzaCancellationException() = runBlocking {
        val api = ApiFake()
        api.errorAlListar = CancellationException("cancelado")

        var relanzada = false
        try {
            OrganizacionesRemoteRepository(api).listar()
        } catch (e: CancellationException) {
            relanzada = true
        }

        assertTrue(relanzada)
    }

    // ---------- mensajeDeError ----------

    @Test
    fun mensajeDeError_httpException_incluyeElCodigo() {
        val mensaje = mensajeDeError(httpException(500))

        assertEquals("El servidor respondió con error 500", mensaje)
    }

    @Test
    fun mensajeDeError_ioException_pideRevisarLaApi() {
        val mensaje = mensajeDeError(IOException("sin red"))

        assertEquals(
            "No se pudo conectar con el servidor. Revisa que la API y MySQL estén encendidos.",
            mensaje
        )
    }

    @Test
    fun mensajeDeError_otraExcepcion_retornaMensajeGenerico() {
        val mensaje = mensajeDeError(IllegalStateException("algo falló"))

        assertEquals("Ocurrió un error inesperado", mensaje)
        assertFalse(mensaje.isEmpty())
    }
}