package cl.duoc.agenciaoportunidades.ui.contactos

import cl.duoc.agenciaoportunidades.model.AreaContacto
import cl.duoc.agenciaoportunidades.model.EstadoContacto
import cl.duoc.agenciaoportunidades.model.MedioContacto

enum class CampoContacto {
    NOMBRE, CARGO, CORREO, TELEFONO
}

data class FormularioContactoUiState(
    // ----- Estado de la pantalla -----
    val cargando: Boolean = true,
    val esEdicion: Boolean = false,
    val noEncontrado: Boolean = false,
    val guardando: Boolean = false,
    val guardadoId: Int? = null,
    val mensajeError: String? = null,

    val organizacionNombre: String = "",

    // ----- Valores ingresados (se conservan si falla una validación) -----
    val nombre: String = "",
    val cargo: String = "",
    val area: AreaContacto = AreaContacto.EVENTOS,
    val correo: String = "",
    val telefono: String = "",
    val medio: MedioContacto = MedioContacto.CORREO,
    val estadoContacto: EstadoContacto = EstadoContacto.ACTIVO,
    val observaciones: String = "",

    // ----- Errores por campo -----
    val errores: Map<CampoContacto, String> = emptyMap()
)