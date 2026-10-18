package cl.duoc.agenciaoportunidades.ui.dashboard

import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Oportunidad
import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.SeguimientoDetalle
import cl.duoc.agenciaoportunidades.util.estaAbierta

data class DashboardUiState(
    val cargando: Boolean = true,
    val ahora: Long = System.currentTimeMillis(),
    val resumenes: List<OportunidadResumen> = emptyList(),
    val eventosProximos: List<OportunidadResumen> = emptyList(),
    val seguimientosAtrasados: List<SeguimientoDetalle> = emptyList(),
    val seguimientosPendientes: List<SeguimientoDetalle> = emptyList(),
    val sinSeguimiento: List<Oportunidad> = emptyList()
) {
    val total: Int get() = resumenes.size

    val abiertas: Int get() = resumenes.count { estaAbierta(it.estado) }

    val confirmadas: Int
        get() = resumenes.count {
            it.estado == EstadoOportunidad.PARTICIPACION_CONFIRMADA ||
                    it.estado == EstadoOportunidad.ACTIVACION_REALIZADA
        }

    val sinAsignar: Int get() = resumenes.count { it.responsableId == null }

    val altaPrioridad: Int
        get() = resumenes.count { estaAbierta(it.estado) && it.prioridad == Prioridad.ALTA }
}