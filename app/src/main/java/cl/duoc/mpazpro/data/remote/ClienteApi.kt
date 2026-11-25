package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.BuildConfig
import cl.duoc.mpazpro.data.Sesion
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Crea el cliente Retrofit de la app (AppContainer lo crea una sola vez).
 * Si la sesión tiene un token del servidor, cada petición lo lleva en «Authorization: Bearer <token>».
 */
object ClienteApi {

    private const val SEGUNDOS_DE_ESPERA = 5L

    fun crear(sesion: Sesion): MpazApi {
        val agregarToken = Interceptor { cadena ->
            val peticion = cadena.request()
            val token = sesion.token.value
            val conToken = if (token != null) {
                peticion.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                peticion
            }
            cadena.proceed(conToken)
        }

        // Tiempos cortos: si el servidor no responde, la app sigue sin esperar demasiado
        val cliente = OkHttpClient.Builder()
            .connectTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .readTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .writeTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .addInterceptor(agregarToken)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MpazApi::class.java)
    }
}
