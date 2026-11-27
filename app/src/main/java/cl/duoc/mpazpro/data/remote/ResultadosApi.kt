package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.data.remote.dto.ActualizarObservacionDto
import cl.duoc.mpazpro.data.remote.dto.IntentoDto
import cl.duoc.mpazpro.data.remote.dto.IntentoServidorDto
import cl.duoc.mpazpro.data.remote.dto.NuevaObservacionDto
import cl.duoc.mpazpro.data.remote.dto.ObservacionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Los endpoints de ms-resultados (puerto 8082): intentos, informes y observaciones. */
interface ResultadosApi {

    @GET("api/salud")
    suspend fun salud(): Map<String, String>

    /** 201 si se guardó, 200 si ya estaba (el servidor no lo duplica). */
    @POST("api/intentos")
    suspend fun enviarIntento(@Body intento: IntentoDto): Response<Unit>

    /** Recibir: 200 con la observación · 404 si no existe. */
    @GET("api/observaciones")
    suspend fun obtenerObservacion(
        @Query("estudiante") estudiante: String,
        @Query("unidadTitulo") unidadTitulo: String
    ): Response<ObservacionDto>

    /** Enviar: 201 creada · 409 si ya existía. */
    @POST("api/observaciones")
    suspend fun crearObservacion(@Body datos: NuevaObservacionDto): Response<ObservacionDto>

    /** Actualizar: 200 · 404 si no existe. */
    @PUT("api/observaciones/{id}")
    suspend fun actualizarObservacion(
        @Path("id") id: Long,
        @Body datos: ActualizarObservacionDto
    ): Response<ObservacionDto>

    /** Los intentos que el servidor tiene de un estudiante (solo docentes y UTP). */
    @GET("api/intentos")
    suspend fun intentosDelEstudiante(@Query("estudiante") estudiante: String): List<IntentoServidorDto>
}
