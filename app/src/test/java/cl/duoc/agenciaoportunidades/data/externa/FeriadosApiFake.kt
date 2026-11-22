package cl.duoc.agenciaoportunidades.data.externa

// Servidor falso de feriados para probar el ViewModel sin internet
class FeriadosApiFake : FeriadosApiService {

    // Feriados que "devuelve el servidor" según el año consultado
    val respuestas = mutableMapOf<Int, List<FeriadoDto>>()

    // Si se asigna, la próxima llamada lanza este error
    var error: Throwable? = null

    // Registro de las consultas recibidas (año, país)
    val consultas = mutableListOf<Pair<Int, String>>()

    override suspend fun feriados(anio: Int, pais: String): List<FeriadoDto> {
        consultas.add(anio to pais)
        error?.let { throw it }
        return respuestas[anio] ?: emptyList()
    }
}
