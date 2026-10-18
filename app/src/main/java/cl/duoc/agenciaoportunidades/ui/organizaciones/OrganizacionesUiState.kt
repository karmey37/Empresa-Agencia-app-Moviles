package cl.duoc.agenciaoportunidades.ui.organizaciones

import cl.duoc.agenciaoportunidades.model.OrganizacionResumen

data class OrganizacionesUiState(
    val cargando: Boolean = true,
    val organizaciones: List<OrganizacionResumen> = emptyList(),
    val busqueda: String = ""
) {
    // busqueda por nombre, rubro o comuna
    val filtradas: List<OrganizacionResumen>
        get() {
            val texto = busqueda.trim().lowercase()
            return organizaciones.filter { organizacion ->
                texto.isEmpty() ||
                        organizacion.nombre.lowercase().contains(texto) ||
                        organizacion.rubro.lowercase().contains(texto) ||
                        organizacion.comuna.lowercase().contains(texto)
            }
        }
}