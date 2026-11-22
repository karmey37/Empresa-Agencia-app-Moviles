package cl.duoc.agenciaoportunidades.data.externa

import retrofit2.http.GET
import retrofit2.http.Path

// API externa: https://date.nager.at (pública, gratuita y sin API key)
interface FeriadosApiService {

    // GET https://date.nager.at/api/v3/PublicHolidays/2026/CL
    @GET("api/v3/PublicHolidays/{anio}/{pais}")
    suspend fun feriados(
        @Path("anio") anio: Int,
        @Path("pais") pais: String
    ): List<FeriadoDto>
}
