package cl.duoc.agenciaoportunidades.ui.detalle

import cl.duoc.agenciaoportunidades.model.HistorialEstado
import cl.duoc.agenciaoportunidades.model.OportunidadDetalle
import cl.duoc.agenciaoportunidades.model.SeguimientoDetalle
import cl.duoc.agenciaoportunidades.model.Usuario

data class DetalleUiState(
    val cargando: Boolean = true,
    val detalle: OportunidadDetalle? = null,
    val seguimientos: List<SeguimientoDetalle> = emptyList(),
    val historialEstados: List<HistorialEstado> = emptyList(),
    val ejecutivos: List<Usuario> = emptyList(),
    val sinAcceso: Boolean = false,
    // Mensaje de confirmación o error que se muestra una vez (Snackbar)
    val mensaje: String? = null
)