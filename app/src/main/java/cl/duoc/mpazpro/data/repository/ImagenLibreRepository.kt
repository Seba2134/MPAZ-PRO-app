package cl.duoc.mpazpro.data.repository

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import cl.duoc.mpazpro.data.remote.ClienteWikimedia
import cl.duoc.mpazpro.data.remote.dto.RespuestaWikimediaDto
import cl.duoc.mpazpro.domain.model.ImagenLibre
import cl.duoc.mpazpro.domain.model.licenciaDesde
import cl.duoc.mpazpro.domain.model.limpiarAutor
import cl.duoc.mpazpro.domain.model.tituloImagen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import retrofit2.HttpException
import java.io.File
import java.io.IOException

/** Qué pasó al buscar imágenes libres. */
sealed class ResultadoBusqueda {
    data class Encontradas(val imagenes: List<ImagenLibre>) : ResultadoBusqueda()
    data object SinResultados : ResultadoBusqueda()
    data object SinConexion : ResultadoBusqueda()
}

/** Solo estos formatos sirven como imagen de un contenido (se descartan gif, video, pdf…). */
private val MIME_ACEPTADOS = setOf("image/jpeg", "image/png", "image/svg+xml")

/**
 * Convierte la respuesta de Commons en la lista que se muestra: ordenada por el orden de la búsqueda,
 * solo formatos aceptados y solo con una licencia reconocible. Es una función pura (se prueba con Gson, sin red).
 */
fun imagenesDesde(respuesta: RespuestaWikimediaDto): List<ImagenLibre> =
    respuesta.query?.pages.orEmpty()
        .sortedBy { it.index ?: Int.MAX_VALUE }
        .mapNotNull { pagina ->
            val info = pagina.imageinfo?.firstOrNull() ?: return@mapNotNull null
            val miniatura = info.thumburl ?: return@mapNotNull null
            if (info.mime !in MIME_ACEPTADOS) return@mapNotNull null
            val textoLicencia = info.extmetadata?.licenseShortName?.value?.trim().orEmpty()
            val licencia = licenciaDesde(textoLicencia) ?: return@mapNotNull null
            ImagenLibre(
                id = pagina.pageid?.toString() ?: miniatura,
                titulo = tituloImagen(info.extmetadata?.objectName?.value, pagina.title),
                urlMiniatura = miniatura,
                autor = limpiarAutor(info.extmetadata?.artist?.value.orEmpty()),
                textoLicencia = textoLicencia,
                licencia = licencia
            )
        }

/**
 * Busca imágenes libres en Wikimedia Commons (API externa, ajena a nuestros microservicios)
 * y las descarga. Todo corre en Dispatchers.IO y ninguna función lanza errores hacia la pantalla.
 */
class ImagenLibreRepository(
    private val cliente: ClienteWikimedia
) {

    suspend fun buscar(texto: String): ResultadoBusqueda = withContext(Dispatchers.IO) {
        try {
            val respuesta = cliente.api.buscarImagenes(
                texto = texto.trim(),
                action = "query",
                format = "json",
                formatversion = 2,
                generator = "search",
                gsrnamespace = 6,          // 6 = archivos
                gsrlimit = 12,
                prop = "imageinfo",
                iiprop = "url|mime|extmetadata",
                iiurlwidth = 640,
                iiextmetadatafilter = "Artist|LicenseShortName|ObjectName",
                iiextmetadatalanguage = "es"
            )
            val imagenes = imagenesDesde(respuesta)
            if (imagenes.isEmpty()) ResultadoBusqueda.SinResultados else ResultadoBusqueda.Encontradas(imagenes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión con Wikimedia Commons", e)
            ResultadoBusqueda.SinConexion
        } catch (e: HttpException) {
            Log.w(TAG, "Wikimedia Commons respondió ${e.code()}")
            ResultadoBusqueda.SinConexion
        } catch (e: RuntimeException) {
            Log.w(TAG, "Respuesta de Wikimedia Commons que no se pudo leer", e)
            ResultadoBusqueda.SinConexion
        }
    }

    /** Descarga la miniatura y la decodifica reducida (lado máximo 300 px). null si falla. */
    suspend fun miniatura(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
        val bytes = descargarBytes(url) ?: return@withContext null
        try {
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, medidas)
            if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return@withContext null
            var escala = 1
            while (medidas.outWidth / escala > LADO_MINIATURA || medidas.outHeight / escala > LADO_MINIATURA) {
                escala *= 2
            }
            val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)?.asImageBitmap()
        } catch (e: RuntimeException) {
            null
        }
    }

    /** Guarda la imagen (640 px) en [destino]. Si falla, borra el archivo a medias y devuelve false. */
    suspend fun descargarEn(url: String, destino: File): Boolean = withContext(Dispatchers.IO) {
        try {
            cliente.http.newCall(Request.Builder().url(url).build()).execute().use { respuesta ->
                val cuerpo = respuesta.body
                if (!respuesta.isSuccessful || cuerpo == null) {
                    destino.delete()
                    return@withContext false
                }
                destino.outputStream().use { salida -> cuerpo.byteStream().copyTo(salida) }
            }
            if (destino.length() > 0) true else {
                destino.delete()
                false
            }
        } catch (e: CancellationException) {
            destino.delete()
            throw e
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo descargar la imagen", e)
            destino.delete()
            false
        } catch (e: RuntimeException) {
            destino.delete()
            false
        }
    }

    private fun descargarBytes(url: String): ByteArray? = try {
        cliente.http.newCall(Request.Builder().url(url).build()).execute().use { respuesta ->
            if (respuesta.isSuccessful) respuesta.body?.bytes() else null
        }
    } catch (e: IOException) {
        null
    } catch (e: RuntimeException) {
        null
    }

    private companion object {
        const val TAG = "ImagenLibre"
        const val LADO_MINIATURA = 300
    }
}
