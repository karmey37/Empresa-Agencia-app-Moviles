package cl.duoc.agenciaoportunidades.data.remote

import retrofit2.HttpException
import java.io.IOException

class OrganizacionesRemoteRepository(
    private val api: AgenciaApiService = ApiClient.service
) {

    suspend fun listar(): Result<List<OrganizacionDto>> =
        runCatching { api.listar() }

    suspend fun crear(organizacion: OrganizacionDto): Result<OrganizacionDto> =
        runCatching { api.crear(organizacion) }

    suspend fun actualizar(id: Long, organizacion: OrganizacionDto): Result<OrganizacionDto> =
        runCatching { api.actualizar(id, organizacion) }

    suspend fun borrar(id: Long): Result<Unit> = runCatching {
        val respuesta = api.borrar(id)
        if (!respuesta.isSuccessful) {
            throw IllegalStateException("Error ${respuesta.code()}")
        }
    }
}

// Convierte una excepción técnica en un texto entendible para el usuario
fun mensajeDeError(error: Throwable): String = when (error) {
    is HttpException -> "El servidor respondio con error ${error.code()}"
    is IOException -> "No se pudo conectar con el servidor. Revisa que la API y MySQL esten encendidos."
    else -> "Ocurrio un error inesperado"
}