package cl.duoc.agenciaoportunidades.ui.feriados

import cl.duoc.agenciaoportunidades.data.externa.FeriadoDto

data class FeriadosUiState(
    val anio: Int,
    val hoy: String,
    val cargando: Boolean = true,
    val feriados: List<FeriadoDto> = emptyList(),
    val mensaje: String? = null
) {
    // Solo tiene sentido destacar el "próximo feriado" cuando se mira el año actual
    val proximo: FeriadoDto?
        get() = if (anio == hoy.take(4).toIntOrNull()) proximoFeriado(feriados, hoy) else null
}
