package cl.duoc.agenciaoportunidades.data.remote

import retrofit2.Response

class ApiFake : AgenciaApiService {

    // "Base de datos" del servidor falso
    val organizaciones = mutableListOf(
        OrganizacionDto(1L, "Retail Andino S.A.", "EMPRESA", "Retail", "Santiago"),
        OrganizacionDto(2L, "Municipalidad de Puente Alto", "MUNICIPALIDAD", "Gobierno local", "Puente Alto")
    )

    // Errores que cada prueba puede activar
    var errorAlListar: Throwable? = null
    var errorAlGuardar: Throwable? = null
    var errorAlBorrar: Throwable? = null

    // Contadores y datos de la última llamada
    var listados = 0
    var creaciones = 0
    var actualizaciones = 0
    var borrados = 0
    var ultimaCreada: OrganizacionDto? = null
    var ultimaActualizada: OrganizacionDto? = null
    var ultimoIdActualizado: Long? = null
    var ultimoIdBorrado: Long? = null

    override suspend fun listar(): List<OrganizacionDto> {
        listados++
        errorAlListar?.let { throw it }
        return organizaciones.toList()
    }

    override suspend fun obtener(id: Long): OrganizacionDto {
        return organizaciones.first { it.id == id }
    }

    override suspend fun crear(organizacion: OrganizacionDto): OrganizacionDto {
        creaciones++
        errorAlGuardar?.let { throw it }
        ultimaCreada = organizacion
        val nuevoId = (organizaciones.maxOfOrNull { it.id ?: 0L } ?: 0L) + 1
        val nueva = organizacion.copy(id = nuevoId)
        organizaciones.add(nueva)
        return nueva
    }

    override suspend fun actualizar(id: Long, organizacion: OrganizacionDto): OrganizacionDto {
        actualizaciones++
        errorAlGuardar?.let { throw it }
        ultimoIdActualizado = id
        ultimaActualizada = organizacion
        val actualizada = organizacion.copy(id = id)
        val posicion = organizaciones.indexOfFirst { it.id == id }
        if (posicion >= 0) organizaciones[posicion] = actualizada
        return actualizada
    }

    override suspend fun borrar(id: Long): Response<Unit> {
        borrados++
        errorAlBorrar?.let { throw it }
        ultimoIdBorrado = id
        organizaciones.removeAll { it.id == id }
        return Response.success(Unit)
    }
}