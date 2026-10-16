package cl.duoc.agenciaoportunidades.model

//fila del listado de oportunidades (une oportunidad + evento + comuna + organización + responsable)
data class OportunidadResumen(
    val id: Int,
    val nombre: String,
    val estado: EstadoOportunidad,
    val prioridad: Prioridad,
    val tipo: TipoEvento,
    val fechaInicio: Long,
    val comuna: String,
    val region: String,
    val organizacion: String,
    val responsableId: Int?,
    val responsable: String?,
    val fechaActualizacion: Long
)

//fila del historial de seguimientos (une seguimiento + oportunidad + usuario + contacto)
data class SeguimientoDetalle(
    val id: Int,
    val oportunidadId: Int,
    val oportunidadNombre: String,
    val responsableId: Int?,
    val tipoAccion: TipoAccion,
    val fechaHora: Long,
    val resultado: String,
    val proximaAccion: String,
    val fechaProximoSeguimiento: Long?,
    val observaciones: String,
    val usuario: String?,
    val contacto: String?
)