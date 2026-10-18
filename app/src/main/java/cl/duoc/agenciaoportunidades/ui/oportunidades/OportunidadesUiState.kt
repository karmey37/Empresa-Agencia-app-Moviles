package cl.duoc.agenciaoportunidades.ui.oportunidades

import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import cl.duoc.agenciaoportunidades.model.Prioridad

data class OportunidadesUiState(
    val cargando: Boolean = true,
    val oportunidades: List<OportunidadResumen> = emptyList(),
    val busqueda: String = "",
    val estadoFiltro: EstadoOportunidad? = null,
    val prioridadFiltro: Prioridad? = null
) {
    //lista que se muestra: aplica busqueda y filtros sobre las oportunidades del usuario
    val filtradas: List<OportunidadResumen>
        get() {
            val texto = busqueda.trim().lowercase()
            return oportunidades.filter { oportunidad ->
                val coincideTexto = texto.isEmpty() ||
                        oportunidad.nombre.lowercase().contains(texto) ||
                        oportunidad.organizacion.lowercase().contains(texto) ||
                        oportunidad.comuna.lowercase().contains(texto)
                val coincideEstado = estadoFiltro == null || oportunidad.estado == estadoFiltro
                val coincidePrioridad = prioridadFiltro == null || oportunidad.prioridad == prioridadFiltro
                coincideTexto && coincideEstado && coincidePrioridad
            }
        }
}