package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.BuildConfig
import cl.duoc.mpazpro.data.Sesion
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Las dos APIs de nuestros microservicios. Comparten el mismo cliente HTTP. */
data class ApisMicroservicios(
    val usuarios: UsuariosApi,
    val resultados: ResultadosApi
)

/** La API de Wikimedia Commons y el cliente HTTP con que también se descargan las imágenes. */
class ClienteWikimedia(
    val api: WikimediaApi,
    val http: OkHttpClient
)

/**
 * Crea los clientes Retrofit de la app (AppContainer los crea una sola vez).
 * Para nuestros microservicios, si la sesión tiene un token, cada petición lo lleva en «Authorization: Bearer <token>».
 */
object ClienteApi {

    private const val SEGUNDOS_DE_ESPERA = 5L

    /** ms-usuarios (8081) y ms-resultados (8082), con el mismo OkHttpClient y distinta dirección. */
    fun crear(sesion: Sesion): ApisMicroservicios {
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

        // Tiempos cortos: si un servidor no responde, la app sigue sin esperar demasiado
        val cliente = OkHttpClient.Builder()
            .connectTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .readTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .writeTimeout(SEGUNDOS_DE_ESPERA, TimeUnit.SECONDS)
            .addInterceptor(agregarToken)
            .build()

        return ApisMicroservicios(
            usuarios = retrofit(BuildConfig.URL_USUARIOS, cliente).create(UsuariosApi::class.java),
            resultados = retrofit(BuildConfig.URL_RESULTADOS, cliente).create(ResultadosApi::class.java)
        )
    }

    /**
     * Cliente aparte para Wikimedia Commons (API externa): su propio OkHttpClient, con un User-Agent que
     * identifica a la app (lo exige la política de Wikimedia) y SIN el interceptor del token: el JWT de
     * nuestros microservicios nunca se envía a una API externa. Con el mismo cliente se descargan las imágenes.
     */
    fun crearWikimedia(): ClienteWikimedia {
        val agregarUserAgent = Interceptor { cadena ->
            cadena.proceed(cadena.request().newBuilder().header("User-Agent", USER_AGENT).build())
        }
        val cliente = OkHttpClient.Builder()
            .connectTimeout(SEGUNDOS_WIKIMEDIA, TimeUnit.SECONDS)
            .readTimeout(SEGUNDOS_WIKIMEDIA, TimeUnit.SECONDS)
            .writeTimeout(SEGUNDOS_WIKIMEDIA, TimeUnit.SECONDS)
            .addInterceptor(agregarUserAgent)
            .build()
        return ClienteWikimedia(
            api = retrofit(URL_WIKIMEDIA, cliente).create(WikimediaApi::class.java),
            http = cliente
        )
    }

    private const val URL_WIKIMEDIA = "https://commons.wikimedia.org/"
    private const val USER_AGENT = "MPAZPRO/1.0 (proyecto academico Duoc UC)"
    private const val SEGUNDOS_WIKIMEDIA = 10L

    private fun retrofit(baseUrl: String, cliente: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
