package cl.duoc.agenciaoportunidades.ui.equipo

import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import cl.duoc.agenciaoportunidades.model.Usuario

data class ResumenEjecutivo(
    val ejecutivo: Usuario,
    val asignadas: Int,
    val abiertas: Int,
    val altaPrioridad: Int,
    val atrasadas: Int
)

data class EquipoUiState(
    val cargando: Boolean = true,
    val sinAcceso: Boolean = false,
    val ejecutivos: List<ResumenEjecutivo> = emptyList(),
    val sinAsignar: List<OportunidadResumen> = emptyList(),
    val totalOportunidades: Int = 0,
    val totalAtrasadas: Int = 0,
    val mensaje: String? = null
)