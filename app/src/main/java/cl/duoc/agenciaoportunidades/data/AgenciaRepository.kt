package cl.duoc.agenciaoportunidades.data

import cl.duoc.agenciaoportunidades.model.Comuna
import cl.duoc.agenciaoportunidades.model.Contacto
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Evento
import cl.duoc.agenciaoportunidades.model.Evidencia
import cl.duoc.agenciaoportunidades.model.HistorialEstado
import cl.duoc.agenciaoportunidades.model.Oportunidad
import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import cl.duoc.agenciaoportunidades.model.Organizacion
import cl.duoc.agenciaoportunidades.model.PerfilUsuario
import cl.duoc.agenciaoportunidades.model.ResultadoActivacion
import cl.duoc.agenciaoportunidades.model.Seguimiento
import cl.duoc.agenciaoportunidades.model.SeguimientoDetalle
import cl.duoc.agenciaoportunidades.model.Usuario
import kotlinx.coroutines.flow.Flow
import cl.duoc.agenciaoportunidades.model.OportunidadDetalle
import cl.duoc.agenciaoportunidades.model.OrganizacionResumen


//repository: intermediario entre los ViewModel y los DAO
class AgenciaRepository(
    private val db: AppDatabase
) {

    // ---------- datos iniciales ----------
    suspend fun sembrarDatosSiEstaVacia() {
        cargarDatosIniciales(db)
    }

    // ---------- usuarios ----------
    suspend fun iniciarSesion(correo: String, password: String): Usuario? {
        return db.usuarioDao().iniciarSesion(correo.trim(), password)
    }

    suspend fun obtenerUsuario(id: Int): Usuario? {
        return db.usuarioDao().obtenerPorId(id)
    }

    fun obtenerEjecutivos(): Flow<List<Usuario>> {
        return db.usuarioDao().obtenerEjecutivos()
    }

    // ---------- comunas ----------
    fun obtenerComunas(): Flow<List<Comuna>> {
        return db.comunaDao().obtenerTodas()
    }

    // ---------- organizaciones ----------
    fun obtenerOrganizaciones(): Flow<List<Organizacion>> {
        return db.organizacionDao().obtenerTodas()
    }
    fun observarOrganizacion(id: Int): Flow<Organizacion?> {
        return db.organizacionDao().observarPorId(id)
    }

    fun obtenerResumenesOrganizaciones(): Flow<List<OrganizacionResumen>> {
        return db.organizacionDao().obtenerResumenes()
    }

    fun obtenerEventosPorOrganizacion(organizacionId: Int): Flow<List<Evento>> {
        return db.eventoDao().obtenerPorOrganizacion(organizacionId)
    }

    suspend fun obtenerOrganizacion(id: Int): Organizacion? {
        return db.organizacionDao().obtenerPorId(id)
    }

    suspend fun guardarOrganizacion(organizacion: Organizacion): Int {
        return db.organizacionDao().insertar(organizacion).toInt()
    }

    suspend fun actualizarOrganizacion(organizacion: Organizacion) {
        db.organizacionDao().actualizar(organizacion)
    }

    // ---------- contactos ----------
    fun obtenerContactos(): Flow<List<Contacto>> {
        return db.contactoDao().obtenerTodos()
    }

    fun obtenerContactosPorOrganizacion(organizacionId: Int): Flow<List<Contacto>> {
        return db.contactoDao().obtenerPorOrganizacion(organizacionId)
    }

    suspend fun obtenerContacto(id: Int): Contacto? {
        return db.contactoDao().obtenerPorId(id)
    }

    suspend fun guardarContacto(contacto: Contacto): Int {
        return db.contactoDao().insertar(contacto).toInt()
    }

    suspend fun actualizarContacto(contacto: Contacto) {
        db.contactoDao().actualizar(contacto)
    }

    // ---------- eventos ----------
    fun obtenerEventos(): Flow<List<Evento>> {
        return db.eventoDao().obtenerTodos()
    }

    suspend fun obtenerEvento(id: Int): Evento? {
        return db.eventoDao().obtenerPorId(id)
    }

    suspend fun guardarEvento(evento: Evento): Int {
        return db.eventoDao().insertar(evento).toInt()
    }

    suspend fun actualizarEvento(evento: Evento) {
        db.eventoDao().actualizar(evento)
    }

    // ---------- oportunidades ----------

    // Supervisor ve todas; ejecutivo solo las que tiene asignadas
    fun obtenerResumenes(usuario: Usuario): Flow<List<OportunidadResumen>> {
        return if (usuario.perfil == PerfilUsuario.SUPERVISOR) {
            db.oportunidadDao().obtenerResumenes()
        } else {
            db.oportunidadDao().obtenerResumenesPorResponsable(usuario.id)
        }
    }

    fun observarOportunidad(id: Int): Flow<Oportunidad?> {
        return db.oportunidadDao().observarPorId(id)
    }

    fun observarDetalleOportunidad(id: Int): Flow<OportunidadDetalle?> {
        return db.oportunidadDao().observarDetalle(id)
    }

    suspend fun obtenerOportunidad(id: Int): Oportunidad? {
        return db.oportunidadDao().obtenerPorId(id)
    }

    fun obtenerSinSeguimiento(): Flow<List<Oportunidad>> {
        return db.oportunidadDao().obtenerSinSeguimiento()
    }

    //crea la oportunidad y deja el primer registro en el historial de estados
    suspend fun crearOportunidad(oportunidad: Oportunidad, usuarioId: Int): Int {
        val id = db.oportunidadDao().insertar(oportunidad).toInt()
        db.historialEstadoDao().insertar(
            HistorialEstado(
                oportunidadId = id,
                estadoAnterior = null,
                estadoNuevo = oportunidad.estado,
                usuarioId = usuarioId,
                observaciones = "Oportunidad creada"
            )
        )
        return id
    }

    suspend fun actualizarOportunidad(oportunidad: Oportunidad) {
        db.oportunidadDao().actualizar(
            oportunidad.copy(fechaActualizacion = System.currentTimeMillis())
        )
    }

    //cambia el estado y guarda el cambio en el historial
    suspend fun cambiarEstado(
        oportunidad: Oportunidad,
        nuevoEstado: EstadoOportunidad,
        usuarioId: Int,
        observaciones: String = ""
    ) {
        db.oportunidadDao().actualizar(
            oportunidad.copy(
                estado = nuevoEstado,
                fechaActualizacion = System.currentTimeMillis()
            )
        )
        db.historialEstadoDao().insertar(
            HistorialEstado(
                oportunidadId = oportunidad.id,
                estadoAnterior = oportunidad.estado,
                estadoNuevo = nuevoEstado,
                usuarioId = usuarioId,
                observaciones = observaciones
            )
        )
    }

    //asigna o reasigna el responsable (null = dejarla sin asignar)
    suspend fun asignarResponsable(oportunidad: Oportunidad, responsableId: Int?) {
        db.oportunidadDao().actualizar(
            oportunidad.copy(
                responsableId = responsableId,
                fechaActualizacion = System.currentTimeMillis()
            )
        )
    }

    fun obtenerHistorialEstados(oportunidadId: Int): Flow<List<HistorialEstado>> {
        return db.historialEstadoDao().obtenerPorOportunidad(oportunidadId)
    }

    // ---------- Seguimientos ----------
    fun obtenerSeguimientos(oportunidadId: Int): Flow<List<SeguimientoDetalle>> {
        return db.seguimientoDao().obtenerPorOportunidad(oportunidadId)
    }

    fun obtenerSeguimientosAtrasados(): Flow<List<SeguimientoDetalle>> {
        return db.seguimientoDao().obtenerAtrasados(System.currentTimeMillis())
    }

    fun obtenerSeguimientosPendientes(): Flow<List<SeguimientoDetalle>> {
        return db.seguimientoDao().obtenerPendientes(System.currentTimeMillis())
    }

    suspend fun registrarSeguimiento(seguimiento: Seguimiento): Int {
        return db.seguimientoDao().insertar(seguimiento).toInt()
    }

    // --------- evidencias (fotos) ----------
    fun obtenerEvidencias(oportunidadId: Int): Flow<List<Evidencia>> {
        return db.evidenciaDao().obtenerPorOportunidad(oportunidadId)
    }

    suspend fun guardarEvidencia(evidencia: Evidencia) {
        db.evidenciaDao().insertar(evidencia)
    }

    suspend fun eliminarEvidencia(evidencia: Evidencia) {
        db.evidenciaDao().eliminar(evidencia)
    }

    // ---------- resultado de la activación ----------
    fun obtenerResultados(oportunidadId: Int): Flow<List<ResultadoActivacion>> {
        return db.resultadoActivacionDao().obtenerPorOportunidad(oportunidadId)
    }

    suspend fun registrarResultado(resultado: ResultadoActivacion) {
        db.resultadoActivacionDao().insertar(resultado)
    }
}