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

//datos completos de una oportunidad para la pantalla de detalle
data class OportunidadDetalle(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val fuente: String,
    val fechaDeteccion: Long,
    val estado: EstadoOportunidad,
    val prioridad: Prioridad,
    val potencialComercial: Prioridad,
    val afinidadPublico: Prioridad,
    val observaciones: String,
    val fechaActualizacion: Long,
    val responsableId: Int?,
    val responsable: String?,
    val eventoNombre: String,
    val tipoEvento: TipoEvento,
    val fechaInicio: Long,
    val fechaTermino: Long,
    val horario: String,
    val ubicacion: String,
    val comuna: String,
    val region: String,
    val publicoEsperado: String,
    val publicoObjetivo: String,
    val asistentesEstimados: Int,
    val condiciones: String,
    val requisitos: String,
    val costoReferencial: Int,
    val espaciosDisponibles: Int,
    val organizacion: String,
    val rubro: String
)

// Fila del listado de organizaciones (une organización + comuna + conteos)
data class OrganizacionResumen(
    val id: Int,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comuna: String,
    val region: String,
    val cantidadContactos: Int,
    val cantidadEventos: Int
)