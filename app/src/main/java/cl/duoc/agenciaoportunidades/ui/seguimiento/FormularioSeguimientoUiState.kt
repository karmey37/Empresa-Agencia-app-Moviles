package cl.duoc.agenciaoportunidades.ui.seguimiento

import cl.duoc.agenciaoportunidades.model.Contacto
import cl.duoc.agenciaoportunidades.model.TipoAccion

enum class CampoSeguimiento {
    FECHA, RESULTADO, PROXIMA_ACCION, FECHA_PROXIMA
}

data class FormularioSeguimientoUiState(
    // ----- Estado de la pantalla -----
    val cargando: Boolean = true,
    val sinPermiso: Boolean = false,
    val noEncontrada: Boolean = false,
    val guardando: Boolean = false,
    val guardadoId: Int? = null,
    val mensajeError: String? = null,

    // ----- Datos de apoyo -----
    val oportunidadNombre: String = "",
    val contactos: List<Contacto> = emptyList(),

    // ----- Valores ingresados (se conservan si falla una validación) -----
    val tipoAccion: TipoAccion = TipoAccion.SEGUIMIENTO,
    val contactoId: Int? = null,
    val fecha: String = "",
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProxima: String = "",
    val observaciones: String = "",

    // ----- errores por campo -----
    val errores: Map<CampoSeguimiento, String> = emptyMap()
)