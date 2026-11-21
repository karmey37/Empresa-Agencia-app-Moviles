package cl.duoc.agenciaoportunidades.ui.directorio

import cl.duoc.agenciaoportunidades.data.remote.ApiFake
import cl.duoc.agenciaoportunidades.data.remote.OrganizacionDto
import cl.duoc.agenciaoportunidades.data.remote.OrganizacionesRemoteRepository
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DirectorioViewModelTest {

    private lateinit var api: ApiFake
    private val dispatcher = StandardTestDispatcher()

    private val mensajeSinConexion =
        "No se pudo conectar con el servidor. Revisa que la API y MySQL estén encendidos."

    @Before
    fun preparar() {
        // viewModelScope usa el hilo principal de Android, que no existe en una prueba JVM
        Dispatchers.setMain(dispatcher)
        api = ApiFake()
    }

    @After
    fun finalizar() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel() =
        DirectorioViewModel(OrganizacionesRemoteRepository(api))

    // Crea el ViewModel y deja terminar la carga inicial
    private fun TestScope.viewModelListo(): DirectorioViewModel {
        val viewModel = crearViewModel()
        advanceUntilIdle()
        return viewModel
    }

    // Completa los campos obligatorios del formulario
    private fun DirectorioViewModel.llenarFormularioValido() {
        actualizar { it.copy(nombre = "  Centro Comercial Test  ") }
        actualizar { it.copy(rubro = "Comercio") }
        actualizar { it.copy(comuna = "Maipú") }
    }

    // ---------- carga ----------

    @Test
    fun estadoInicial_estaCargandoAntesDeLaRespuesta() = runTest {
        val viewModel = crearViewModel()

        assertTrue(viewModel.uiState.value.cargando)
    }

    @Test
    fun cargaInicial_obtieneLasOrganizacionesOrdenadasPorNombre() = runTest {
        val viewModel = viewModelListo()

        val ui = viewModel.uiState.value
        assertFalse(ui.cargando)
        assertEquals(listOf(2L, 1L), ui.organizaciones.map { it.id })
        assertEquals(1, api.listados)
    }

    @Test
    fun cargaInicial_sinConexion_muestraMensajeDeError() = runTest {
        api.errorAlListar = IOException("sin red")

        val viewModel = viewModelListo()

        val ui = viewModel.uiState.value
        assertFalse(ui.cargando)
        assertTrue(ui.organizaciones.isEmpty())
        assertEquals(mensajeSinConexion, ui.mensaje)
    }

    @Test
    fun cargar_denuevo_realizaUnaNuevaConsulta() = runTest {
        val viewModel = viewModelListo()

        viewModel.cargar()
        advanceUntilIdle()

        assertEquals(2, api.listados)
    }

    @Test
    fun consumirMensaje_limpiaElMensaje() = runTest {
        api.errorAlListar = IOException("sin red")
        val viewModel = viewModelListo()

        viewModel.consumirMensaje()

        assertNull(viewModel.uiState.value.mensaje)
    }

    // ---------- búsqueda ----------

    @Test
    fun cambiarBusqueda_filtraPorNombre() = runTest {
        val viewModel = viewModelListo()

        viewModel.cambiarBusqueda("retail")

        assertEquals(listOf(1L), viewModel.uiState.value.filtradas.map { it.id })
    }

    @Test
    fun cambiarBusqueda_filtraPorComuna() = runTest {
        val viewModel = viewModelListo()

        viewModel.cambiarBusqueda("puente")

        assertEquals(listOf(2L), viewModel.uiState.value.filtradas.map { it.id })
    }

    @Test
    fun cambiarBusqueda_sinCoincidencias_retornaListaVacia() = runTest {
        val viewModel = viewModelListo()

        viewModel.cambiarBusqueda("zzz")

        assertTrue(viewModel.uiState.value.filtradas.isEmpty())
    }

    @Test
    fun cambiarBusqueda_vacia_muestraTodas() = runTest {
        val viewModel = viewModelListo()
        viewModel.cambiarBusqueda("retail")

        viewModel.cambiarBusqueda("")

        assertEquals(2, viewModel.uiState.value.filtradas.size)
    }

    // ---------- abrir y cerrar el formulario ----------

    @Test
    fun abrirNuevo_abreFormularioVacio() = runTest {
        val viewModel = viewModelListo()
        // Primero editamos una para comprobar que el formulario nuevo se limpia
        viewModel.abrirEditar(viewModel.uiState.value.organizaciones.first())

        viewModel.abrirNuevo()

        val ui = viewModel.uiState.value
        assertTrue(ui.formularioAbierto)
        assertNull(ui.editandoId)
        assertEquals("", ui.nombre)
        assertEquals("", ui.rubro)
        assertEquals("", ui.comuna)
        assertEquals(TipoOrganizacion.EMPRESA, ui.tipo)
    }

    @Test
    fun abrirEditar_cargaLosDatosDeLaOrganizacion() = runTest {
        val viewModel = viewModelListo()
        val retail = viewModel.uiState.value.organizaciones.first { it.id == 1L }

        viewModel.abrirEditar(retail)

        val ui = viewModel.uiState.value
        assertTrue(ui.formularioAbierto)
        assertEquals(1L, ui.editandoId)
        assertEquals("Retail Andino S.A.", ui.nombre)
        assertEquals("Retail", ui.rubro)
        assertEquals("Santiago", ui.comuna)
        assertEquals(TipoOrganizacion.EMPRESA, ui.tipo)
        assertEquals("", ui.sitioWeb)
    }

    @Test
    fun abrirEditar_conTipoDesconocido_usaOtra() = runTest {
        val viewModel = viewModelListo()
        val rara = OrganizacionDto(5L, "Org rara", "TIPO_INEXISTENTE", "Rubro", "Comuna")

        viewModel.abrirEditar(rara)

        assertEquals(TipoOrganizacion.OTRA, viewModel.uiState.value.tipo)
    }

    @Test
    fun cerrarFormulario_cierraYLimpiaErrores() = runTest {
        val viewModel = viewModelListo()
        viewModel.abrirNuevo()
        viewModel.guardar()

        viewModel.cerrarFormulario()

        val ui = viewModel.uiState.value
        assertFalse(ui.formularioAbierto)
        assertTrue(ui.errores.isEmpty())
    }

    @Test
    fun actualizar_cambiaElCampoYLimpiaSoloSuError() = runTest {
        val viewModel = viewModelListo()
        viewModel.abrirNuevo()
        viewModel.guardar()
        assertTrue(viewModel.uiState.value.errores.containsKey(CampoDirectorio.NOMBRE))

        viewModel.actualizar(CampoDirectorio.NOMBRE) { it.copy(nombre = "Centro Test") }

        val ui = viewModel.uiState.value
        assertEquals("Centro Test", ui.nombre)
        assertFalse(ui.errores.containsKey(CampoDirectorio.NOMBRE))
        assertTrue(ui.errores.containsKey(CampoDirectorio.RUBRO))
    }

    // ---------- guardar (POST y PUT) ----------

    @Test
    fun guardar_conDatosInvalidos_muestraErroresYNoLlamaALaApi() = runTest {
        val viewModel = viewModelListo()
        viewModel.abrirNuevo()

        viewModel.guardar()
        advanceUntilIdle()

        val ui = viewModel.uiState.value
        assertEquals(
            setOf(CampoDirectorio.NOMBRE, CampoDirectorio.RUBRO, CampoDirectorio.COMUNA),
            ui.errores.keys
        )
        assertEquals(0, api.creaciones)
        assertTrue(ui.formularioAbierto)
        assertFalse(ui.guardando)
    }

    @Test
    fun guardar_organizacionNueva_creaYRecargaLaLista() = runTest {
        val viewModel = viewModelListo()
        viewModel.abrirNuevo()
        viewModel.llenarFormularioValido()

        viewModel.guardar()
        advanceUntilIdle()

        val ui = viewModel.uiState.value
        assertEquals(1, api.creaciones)
        assertEquals("Centro Comercial Test", api.ultimaCreada?.nombre)
        assertEquals("EMPRESA", api.ultimaCreada?.tipo)
        assertFalse(ui.formularioAbierto)
        assertFalse(ui.guardando)
        assertEquals("Organización creada en el servidor", ui.mensaje)
        assertEquals(2, api.listados)
        assertEquals(3, ui.organizaciones.size)
    }

    @Test
    fun guardar_organizacionExistente_actualizaConElIdCorrecto() = runTest {
        val viewModel = viewModelListo()
        val retail = viewModel.uiState.value.organizaciones.first { it.id == 1L }
        viewModel.abrirEditar(retail)
        viewModel.actualizar { it.copy(rubro = "Retail y moda") }

        viewModel.guardar()
        advanceUntilIdle()

        val ui = viewModel.uiState.value
        assertEquals(1, api.actualizaciones)
        assertEquals(1L, api.ultimoIdActualizado)
        assertEquals("Retail y moda", api.ultimaActualizada?.rubro)
        assertFalse(ui.formularioAbierto)
        assertEquals("Organización actualizada en el servidor", ui.mensaje)
    }

    @Test
    fun guardar_conErrorDeServidor_mantieneElFormularioAbierto() = runTest {
        val viewModel = viewModelListo()
        viewModel.abrirNuevo()
        viewModel.llenarFormularioValido()
        api.errorAlGuardar = IOException("sin red")

        viewModel.guardar()
        advanceUntilIdle()

        val ui = viewModel.uiState.value
        assertTrue(ui.formularioAbierto)
        assertFalse(ui.guardando)
        assertEquals(mensajeSinConexion, ui.mensaje)
    }

    @Test
    fun guardar_dosVecesSeguidas_soloEnviaUnaPeticion() = runTest {
        val viewModel = viewModelListo()
        viewModel.abrirNuevo()
        viewModel.llenarFormularioValido()

        viewModel.guardar()
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, api.creaciones)
    }

    // ---------- eliminar (DELETE) ----------

    @Test
    fun pedirEliminar_guardaLaOrganizacionPendiente() = runTest {
        val viewModel = viewModelListo()
        val retail = viewModel.uiState.value.organizaciones.first { it.id == 1L }

        viewModel.pedirEliminar(retail)

        assertEquals(retail, viewModel.uiState.value.aEliminar)
    }

    @Test
    fun cancelarEliminar_descartaLaOrganizacionPendiente() = runTest {
        val viewModel = viewModelListo()
        viewModel.pedirEliminar(viewModel.uiState.value.organizaciones.first())

        viewModel.cancelarEliminar()

        assertNull(viewModel.uiState.value.aEliminar)
        assertEquals(0, api.borrados)
    }

    @Test
    fun confirmarEliminar_borraYRecargaLaLista() = runTest {
        val viewModel = viewModelListo()
        val retail = viewModel.uiState.value.organizaciones.first { it.id == 1L }
        viewModel.pedirEliminar(retail)

        viewModel.confirmarEliminar()
        advanceUntilIdle()

        val ui = viewModel.uiState.value
        assertEquals(1, api.borrados)
        assertEquals(1L, api.ultimoIdBorrado)
        assertNull(ui.aEliminar)
        assertEquals("Organización eliminada del servidor", ui.mensaje)
        assertEquals(2, api.listados)
        assertEquals(1, ui.organizaciones.size)
    }

    @Test
    fun confirmarEliminar_sinOrganizacionPendiente_noHaceNada() = runTest {
        val viewModel = viewModelListo()

        viewModel.confirmarEliminar()
        advanceUntilIdle()

        assertEquals(0, api.borrados)
    }

    @Test
    fun confirmarEliminar_organizacionSinId_noLlamaALaApi() = runTest {
        val viewModel = viewModelListo()
        val sinId = OrganizacionDto(
            nombre = "Sin id",
            tipo = "EMPRESA",
            rubro = "Rubro",
            comuna = "Comuna"
        )
        viewModel.pedirEliminar(sinId)

        viewModel.confirmarEliminar()
        advanceUntilIdle()

        assertEquals(0, api.borrados)
    }

    @Test
    fun confirmarEliminar_conErrorDeServidor_muestraMensajeYNoRecarga() = runTest {
        val viewModel = viewModelListo()
        viewModel.pedirEliminar(viewModel.uiState.value.organizaciones.first())
        api.errorAlBorrar = IOException("sin red")

        viewModel.confirmarEliminar()
        advanceUntilIdle()

        val ui = viewModel.uiState.value
        assertEquals(mensajeSinConexion, ui.mensaje)
        assertEquals(1, api.listados)
        assertEquals(2, ui.organizaciones.size)
    }
}