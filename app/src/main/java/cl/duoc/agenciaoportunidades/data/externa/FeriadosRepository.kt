package cl.duoc.agenciaoportunidades.data.externa

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

const val PAIS_CHILE = "CL"

class FeriadosRepository(
    private val api: FeriadosApiService = FeriadosClient.service
) {

    // Devuelve los feriados del año ordenados por fecha. Nunca lanza excepciones de red:
    // los errores vuelven dentro de Result.failure para que el ViewModel los muestre.
    suspend fun obtener(anio: Int, pais: String = PAIS_CHILE): Result<List<FeriadoDto>> =
        try {
            Result.success(api.feriados(anio, pais).sortedBy { it.date })
        } catch (e: CancellationException) {
            // Si la corrutina se cancela (ej: el usuario cambia de año), se relanza
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}

// Convierte una excepción técnica en un texto entendible para el usuario
fun mensajeDeErrorFeriados(error: Throwable): String = when (error) {
    is HttpException -> "El servicio de feriados respondió con error ${error.code()}"
    is IOException -> "No se pudo conectar con el servicio de feriados. Revisa tu conexión a internet."
    else -> "Ocurrió un error inesperado al leer los feriados"
}
