package cl.duoc.agenciaoportunidades.ui.login

import cl.duoc.agenciaoportunidades.model.Usuario

data class LoginUiState(
    // ----- Datos ingresados (se conservan si tirra error alguna validacion) -----
    val correo: String = "",
    val password: String = "",
    val mostrarPassword: Boolean = false,

    // ----- Errores null  -----
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorCredenciales: String? = null,

    // ----- Estado del proceso -----
    val preparando: Boolean = true,
    val cargando: Boolean = false,
    val usuarioAutenticado: Usuario? = null
)