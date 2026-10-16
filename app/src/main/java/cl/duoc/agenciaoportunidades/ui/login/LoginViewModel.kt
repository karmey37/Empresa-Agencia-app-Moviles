package cl.duoc.agenciaoportunidades.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.util.validarCorreo
import cl.duoc.agenciaoportunidades.util.validarPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Al abrir la app se cargan los datos ficticios si la base esta vacía
        viewModelScope.launch {
            repository.sembrarDatosSiEstaVacia()
            _uiState.update { it.copy(preparando = false) }
        }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, errorCredenciales = null) }
    }

    fun cambiarPassword(valor: String) {
        _uiState.update { it.copy(password = valor, errorPassword = null, errorCredenciales = null) }
    }

    fun alternarMostrarPassword() {
        _uiState.update { it.copy(mostrarPassword = !it.mostrarPassword) }
    }

    // Rellena los campos con una cuenta ficticia, (prueba rapida jej)
    fun usarCuentaDemo(correo: String, password: String) {
        _uiState.update {
            it.copy(
                correo = correo,
                password = password,
                errorCorreo = null,
                errorPassword = null,
                errorCredenciales = null
            )
        }
    }

    fun iniciarSesion() {
        val estado = _uiState.value

        // Validamos con las reglas de Validaciones.kt
        val errorCorreo = validarCorreo(estado.correo)
        val errorPassword = validarPassword(estado.password)

        _uiState.update {
            it.copy(
                errorCorreo = errorCorreo,
                errorPassword = errorPassword,
                errorCredenciales = null
            )
        }
        if (errorCorreo != null || errorPassword != null) return

        _uiState.update { it.copy(cargando = true) }

        viewModelScope.launch {
            try {
                val usuario = repository.iniciarSesion(
                    estado.correo.trim().lowercase(),
                    estado.password
                )
                if (usuario == null) {
                    _uiState.update {
                        it.copy(cargando = false, errorCredenciales = "Correo o contraseña incorrectos")
                    }
                } else {
                    _uiState.update { it.copy(cargando = false, usuarioAutenticado = usuario) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorCredenciales = "No se pudo iniciar sesión. Inténtalo nuevamente"
                    )
                }
            }
        }
    }

    // la pantalla avisa que ya hubo un login exitoso
    fun consumirLogin() {
        _uiState.update { it.copy(usuarioAutenticado = null, password = "") }
    }
}