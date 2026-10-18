package cl.duoc.agenciaoportunidades.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.agenciaoportunidades.ui.dashboard.DashboardScreen
import cl.duoc.agenciaoportunidades.ui.detalle.DetalleOportunidadScreen
import cl.duoc.agenciaoportunidades.ui.formulario.FormularioOportunidadScreen
import cl.duoc.agenciaoportunidades.ui.login.LoginScreen
import cl.duoc.agenciaoportunidades.ui.oportunidades.OportunidadesScreen
import cl.duoc.agenciaoportunidades.ui.organizaciones.DetalleOrganizacionScreen
import cl.duoc.agenciaoportunidades.ui.organizaciones.FormularioOrganizacionScreen
import cl.duoc.agenciaoportunidades.ui.organizaciones.OrganizacionesScreen
import cl.duoc.agenciaoportunidades.ui.perfil.PerfilScreen
import cl.duoc.agenciaoportunidades.ui.seguimiento.FormularioSeguimientoScreen
import cl.duoc.agenciaoportunidades.ui.sesion.SesionViewModel

object Rutas {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val OPORTUNIDADES = "oportunidades"
    const val ORGANIZACIONES = "organizaciones"
    const val PERFIL = "perfil"
    const val DETALLE = "detalle"
    const val SEGUIMIENTO = "seguimiento"
    const val ORGANIZACION = "organizacion"
    const val FORMULARIO = "formulario"
    const val FORMULARIO_PATRON = "formulario?oportunidadId={oportunidadId}"
    const val FORMULARIO_ORGANIZACION = "formularioOrganizacion"
    const val FORMULARIO_ORGANIZACION_PATRON = "formularioOrganizacion?organizacionId={organizacionId}"

    fun detalle(oportunidadId: Int): String = "$DETALLE/$oportunidadId"

    fun seguimiento(oportunidadId: Int): String = "$SEGUIMIENTO/$oportunidadId"

    fun organizacion(organizacionId: Int): String = "$ORGANIZACION/$organizacionId"

    // Sin id: crear una oportunidad nueva. Con id: editar esa oportunidad.
    fun formulario(oportunidadId: Int? = null): String {
        return if (oportunidadId == null) FORMULARIO else "$FORMULARIO?oportunidadId=$oportunidadId"
    }

    // Sin id: crear una organización nueva. Con id: editar esa organización.
    fun formularioOrganizacion(organizacionId: Int? = null): String {
        return if (organizacionId == null) {
            FORMULARIO_ORGANIZACION
        } else {
            "$FORMULARIO_ORGANIZACION?organizacionId=$organizacionId"
        }
    }
}

@Composable
fun Navegacion(sesionViewModel: SesionViewModel = viewModel()) {
    val navController = rememberNavController()
    val usuario by sesionViewModel.usuario.collectAsStateWithLifecycle()

    // Cambio de pestaña en la barra inferior: no apila pantallas repetidas
    val irAPestana: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(Rutas.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = { usuarioLogueado ->
                    sesionViewModel.iniciarSesion(usuarioLogueado)
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.DASHBOARD) {
            val actual = usuario
            if (actual != null) {
                DashboardScreen(
                    usuario = actual,
                    onAbrirOportunidad = { id -> navController.navigate(Rutas.detalle(id)) },
                    bottomBar = { BarraInferior(Rutas.DASHBOARD, irAPestana) }
                )
            }
        }
        composable(Rutas.OPORTUNIDADES) {
            val actual = usuario
            if (actual != null) {
                OportunidadesScreen(
                    usuario = actual,
                    onAbrirOportunidad = { id -> navController.navigate(Rutas.detalle(id)) },
                    onNuevaOportunidad = { navController.navigate(Rutas.formulario()) },
                    bottomBar = { BarraInferior(Rutas.OPORTUNIDADES, irAPestana) }
                )
            }
        }
        composable(Rutas.ORGANIZACIONES) {
            OrganizacionesScreen(
                onAbrirOrganizacion = { id -> navController.navigate(Rutas.organizacion(id)) },
                onNuevaOrganizacion = { navController.navigate(Rutas.formularioOrganizacion()) },
                bottomBar = { BarraInferior(Rutas.ORGANIZACIONES, irAPestana) }
            )
        }
        composable(Rutas.PERFIL) {
            val actual = usuario
            if (actual != null) {
                PerfilScreen(
                    usuario = actual,
                    onCerrarSesion = {
                        sesionViewModel.cerrarSesion()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.DASHBOARD) { inclusive = true }
                        }
                    },
                    bottomBar = { BarraInferior(Rutas.PERFIL, irAPestana) }
                )
            }
        }
        composable(
            route = "${Rutas.DETALLE}/{oportunidadId}",
            arguments = listOf(navArgument("oportunidadId") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("oportunidadId")
            val actual = usuario
            if (id != null && actual != null) {
                DetalleOportunidadScreen(
                    oportunidadId = id,
                    usuario = actual,
                    onVolver = { navController.popBackStack() },
                    onEditar = { idEditar -> navController.navigate(Rutas.formulario(idEditar)) },
                    onRegistrarSeguimiento = { idOportunidad ->
                        navController.navigate(Rutas.seguimiento(idOportunidad))
                    }
                )
            }
        }
        composable(
            route = "${Rutas.SEGUIMIENTO}/{oportunidadId}",
            arguments = listOf(navArgument("oportunidadId") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("oportunidadId")
            val actual = usuario
            if (id != null && actual != null) {
                FormularioSeguimientoScreen(
                    oportunidadId = id,
                    usuario = actual,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = Rutas.FORMULARIO_PATRON,
            arguments = listOf(
                navArgument("oportunidadId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { entrada ->
            val idArgumento = entrada.arguments?.getInt("oportunidadId") ?: -1
            val actual = usuario
            if (actual != null) {
                FormularioOportunidadScreen(
                    usuario = actual,
                    oportunidadId = if (idArgumento == -1) null else idArgumento,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { idGuardado, esEdicion ->
                        if (esEdicion) {
                            navController.popBackStack()
                        } else {
                            // Al crear, se abre el detalle y el formulario sale de la pila
                            navController.navigate(Rutas.detalle(idGuardado)) {
                                popUpTo(Rutas.FORMULARIO_PATRON) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
        composable(
            route = "${Rutas.ORGANIZACION}/{organizacionId}",
            arguments = listOf(navArgument("organizacionId") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("organizacionId")
            if (id != null) {
                DetalleOrganizacionScreen(
                    organizacionId = id,
                    onVolver = { navController.popBackStack() },
                    onEditar = { idEditar ->
                        navController.navigate(Rutas.formularioOrganizacion(idEditar))
                    }
                )
            }
        }
        composable(
            route = Rutas.FORMULARIO_ORGANIZACION_PATRON,
            arguments = listOf(
                navArgument("organizacionId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { entrada ->
            val idArgumento = entrada.arguments?.getInt("organizacionId") ?: -1
            FormularioOrganizacionScreen(
                organizacionId = if (idArgumento == -1) null else idArgumento,
                onVolver = { navController.popBackStack() },
                onGuardado = { idGuardado, esEdicion ->
                    if (esEdicion) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Rutas.organizacion(idGuardado)) {
                            popUpTo(Rutas.FORMULARIO_ORGANIZACION_PATRON) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}