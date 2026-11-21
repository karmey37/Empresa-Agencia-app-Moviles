package cl.duoc.agenciaoportunidades.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class OrganizacionesRemoteRepository(
    private val api: AgenciaApiService = ApiClient.service
) {

    suspend fun listar(): Result<List<OrganizacionDto>> =
        llamar { api.listar() }

    suspend fun crear(organizacion: OrganizacionDto): Result<OrganizacionDto> =
        llamar { api.crear(organizacion) }

    suspend fun actualizar(id: Long, organizacion: OrganizacionDto): Result<OrganizacionDto> =
        llamar { api.actualizar(id, organizacion) }

    suspend fun borrar(id: Long): Result<Unit> = llamar {
        val respuesta = api.borrar(id)
        if (!respuesta.isSuccessful) {
            throw HttpException(respuesta)
        }
    }

    // Igual que en clase: si la corrutina se cancela, se relanza la excepción
    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}

// Convierte una excepción técnica en un texto entendible para el usuario
fun mensajeDeError(error: Throwable): String = when (error) {
    is HttpException -> "El servidor respondió con error ${error.code()}"
    is IOException -> "No se pudo conectar con el servidor. Revisa que la API y MySQL estén encendidos."
    else -> "Ocurrió un error inesperado"
}