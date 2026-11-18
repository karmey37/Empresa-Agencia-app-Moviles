package cl.duoc.agenciaoportunidades.ui.directorio

import cl.duoc.agenciaoportunidades.data.remote.OrganizacionDto
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion

enum class CampoDirectorio {
    NOMBRE, RUBRO, COMUNA, SITIO_WEB
}

data class DirectorioUiState(
    // ----- Lista que viene del servidor -----
    val cargando: Boolean = true,
    val organizaciones: List<OrganizacionDto> = emptyList(),
    val busqueda: String = "",
    val mensaje: String? = null,

    // ----- Formulario (crear / editar) -----
    val formularioAbierto: Boolean = false,
    val editandoId: Long? = null,
    val guardando: Boolean = false,
    val nombre: String = "",
    val tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,
    val rubro: String = "",
    val comuna: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = "",
    val errores: Map<CampoDirectorio, String> = emptyMap(),

    // ----- Confirmacion para borrar -----
    val aEliminar: OrganizacionDto? = null
) {
    // busqueda por nombre, rubro o comuna
    val filtradas: List<OrganizacionDto>
        get() {
            val texto = busqueda.trim().lowercase()
            return organizaciones.filter { org ->
                texto.isEmpty() ||
                        org.nombre.lowercase().contains(texto) ||
                        org.rubro.lowercase().contains(texto) ||
                        org.comuna.lowercase().contains(texto)
            }
        }
}