package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.data.remote.dto.RespuestaWikimediaDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API pública de MediaWiki de Wikimedia Commons (https://commons.wikimedia.org/w/api.php).
 * Es gratuita, no necesita clave y no tiene relación con nuestros microservicios.
 * Una sola llamada busca archivos y trae su miniatura, autor y licencia.
 */
interface WikimediaApi {

    @GET("w/api.php")
    suspend fun buscarImagenes(
        @Query("gsrsearch") texto: String,
        @Query("action") action: String,
        @Query("format") format: String,
        @Query("formatversion") formatversion: Int,
        @Query("generator") generator: String,
        @Query("gsrnamespace") gsrnamespace: Int,
        @Query("gsrlimit") gsrlimit: Int,
        @Query("prop") prop: String,
        @Query("iiprop") iiprop: String,
        @Query("iiurlwidth") iiurlwidth: Int,
        @Query("iiextmetadatafilter") iiextmetadatafilter: String,
        @Query("iiextmetadatalanguage") iiextmetadatalanguage: String
    ): RespuestaWikimediaDto
}
