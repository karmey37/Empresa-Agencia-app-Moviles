package cl.duoc.agenciaoportunidades.ui.organizaciones

import cl.duoc.agenciaoportunidades.model.Comuna
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion

enum class CampoOrganizacion {
    NOMBRE, RUBRO, COMUNA, SITIO_WEB
}

data class FormularioOrganizacionUiState(
    // ----- Estado de la pantalla -----
    val cargando: Boolean = true,
    val esEdicion: Boolean = false,
    val noEncontrada: Boolean = false,
    val guardando: Boolean = false,
    val guardadoId: Int? = null,
    val mensajeError: String? = null,

    // ----- Datos para la lista desplegable -----
    val comunas: List<Comuna> = emptyList(),

    // ----- Valores ingresados (se conservan si falla una validación) -----
    val nombre: String = "",
    val tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,
    val rubro: String = "",
    val comunaId: Int? = null,
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = "",

    // ----- Errores por campo -----
    val errores: Map<CampoOrganizacion, String> = emptyMap()
)