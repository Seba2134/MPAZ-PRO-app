package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.data.remote.dto.IntentoDto
import cl.duoc.mpazpro.data.remote.dto.LoginDto
import cl.duoc.mpazpro.data.remote.dto.TokenDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Los endpoints del servidor que usa la app (ver backend/). */
interface MpazApi {

    @GET("api/salud")
    suspend fun salud(): Map<String, String>

    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginDto): TokenDto

    /** 201 si se guardó, 200 si ya estaba (el servidor no lo duplica). */
    @POST("api/intentos")
    suspend fun enviarIntento(@Body intento: IntentoDto): Response<Unit>
}
