package cl.duoc.agenciaoportunidades.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.agenciaoportunidades.ui.dashboard.DashboardScreen
import cl.duoc.agenciaoportunidades.ui.login.LoginScreen
import cl.duoc.agenciaoportunidades.ui.sesion.SesionViewModel

// nombres de las rutas de la app
object Rutas {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
}

@Composable
fun Navegacion(sesionViewModel: SesionViewModel = viewModel()) {
    val navController = rememberNavController()
    val usuario by sesionViewModel.usuario.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = { usuarioLogueado ->
                    sesionViewModel.iniciarSesion(usuarioLogueado)
                    // Saca el login de la pila: el botón "atrás" ya no vuelve a él
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
                    onCerrarSesion = {
                        sesionViewModel.cerrarSesion()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.DASHBOARD) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}