package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.data.remote.dto.LoginDto
import cl.duoc.mpazpro.data.remote.dto.TokenDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Los endpoints de ms-usuarios (puerto 8081): identidad y JWT. */
interface UsuariosApi {

    @GET("api/salud")
    suspend fun salud(): Map<String, String>

    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginDto): TokenDto
}
