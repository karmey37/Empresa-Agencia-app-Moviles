package cl.duoc.agenciaoportunidades.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AgenciaApiService {

    @GET("organizaciones")
    suspend fun listar(): List<OrganizacionDto>

    @GET("organizaciones/{id}")
    suspend fun obtener(@Path("id") id: Long): OrganizacionDto

    @POST("organizaciones")
    suspend fun crear(@Body organizacion: OrganizacionDto): OrganizacionDto

    @PUT("organizaciones/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body organizacion: OrganizacionDto
    ): OrganizacionDto

    @DELETE("organizaciones/{id}")
    suspend fun borrar(@Path("id") id: Long): Response<Unit>
}