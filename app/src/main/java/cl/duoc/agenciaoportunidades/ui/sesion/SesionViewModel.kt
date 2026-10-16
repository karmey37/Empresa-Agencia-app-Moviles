package cl.duoc.agenciaoportunidades.ui.sesion

import androidx.lifecycle.ViewModel
import cl.duoc.agenciaoportunidades.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Guarda quien inicio sesion y con que perfil mientras la app esta abierta
class SesionViewModel : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        _usuario.value = usuario
    }

    fun cerrarSesion() {
        _usuario.value = null
    }
}