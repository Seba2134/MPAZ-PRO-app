package cl.duoc.mpazpro.data.repository

import android.util.Log
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.remote.ResultadosApi
import cl.duoc.mpazpro.data.remote.dto.ActualizarObservacionDto
import cl.duoc.mpazpro.data.remote.dto.IntentoServidorDto
import cl.duoc.mpazpro.data.remote.dto.NuevaObservacionDto
import cl.duoc.mpazpro.data.remote.dto.ObservacionDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection

/** El resultado de hablar con el servidor. Nunca es una excepción: la pantalla decide qué mostrar. */
sealed class ResultadoRemoto<out T> {
    data class Exito<T>(val dato: T) : ResultadoRemoto<T>()

    /** El servidor no respondió (apagado o sin red). */
    data object SinConexion : ResultadoRemoto<Nothing>()

    /** No hay token del servidor, o venció (401): hay que volver a iniciar sesión con el servidor encendido. */
    data object SinSesion : ResultadoRemoto<Nothing>()

    /** El servidor respondió con un error que no es de sesión (400, 403, 500…). */
    data class Error(val mensaje: String) : ResultadoRemoto<Nothing>()
}

/** Qué hacer al guardar una observación en el servidor. */
sealed class AccionObservacion {
    /** No existe allá: se crea (POST). */
    data class Crear(val datos: NuevaObservacionDto) : AccionObservacion()

    /** Ya existe allá: se actualiza con su id (PUT). */
    data class Actualizar(val id: Long, val datos: ActualizarObservacionDto) : AccionObservacion()
}

/**
 * Decide entre crear o actualizar según lo que ya tiene el servidor. Es una función pura (se prueba sin red).
 * El id del servidor no se guarda en Room: se obtiene con un GET cada vez.
 */
fun accionParaGuardar(
    existente: ObservacionDto?,
    estudiante: String,
    unidadTitulo: String,
    texto: String,
    validado: Boolean
): AccionObservacion =
    if (existente == null) {
        AccionObservacion.Crear(NuevaObservacionDto(estudiante, unidadTitulo, texto))
    } else {
        AccionObservacion.Actualizar(existente.id, ActualizarObservacionDto(texto, validado))
    }

/**
 * Observaciones e intentos en ms-resultados: recibir (GET), enviar (POST) y actualizar (PUT).
 * La observación de Room sigue siendo la fuente de verdad; esto es la copia del servidor.
 * Todo corre en Dispatchers.IO y ninguna función lanza errores hacia la pantalla.
 */
class ObservacionRemotaRepository(
    private val api: ResultadosApi,
    private val sesion: Sesion
) {

    /** La observación del servidor para ese estudiante y unidad, o null si allá no existe (404). */
    suspend fun obtener(estudiante: String, unidadTitulo: String): ResultadoRemoto<ObservacionDto?> =
        llamar {
            val respuesta = api.obtenerObservacion(estudiante, unidadTitulo)
            when {
                respuesta.isSuccessful -> ResultadoRemoto.Exito(respuesta.body())
                respuesta.code() == HttpURLConnection.HTTP_NOT_FOUND -> ResultadoRemoto.Exito(null)
                else -> errorDe(respuesta)
            }
        }

    /**
     * Guarda la observación en el servidor: hace GET y, si no existe, POST; si existe, PUT con su id.
     * Si se valida una observación que todavía no estaba allá, se crea (POST) y luego se valida (PUT).
     */
    suspend fun guardar(
        estudiante: String,
        unidadTitulo: String,
        texto: String,
        validado: Boolean
    ): ResultadoRemoto<ObservacionDto> {
        val existente = when (val actual = obtener(estudiante, unidadTitulo)) {
            is ResultadoRemoto.Exito -> actual.dato
            is ResultadoRemoto.Error -> return actual
            ResultadoRemoto.SinConexion -> return ResultadoRemoto.SinConexion
            ResultadoRemoto.SinSesion -> return ResultadoRemoto.SinSesion
        }
        return when (val accion = accionParaGuardar(existente, estudiante, unidadTitulo, texto, validado)) {
            is AccionObservacion.Crear -> {
                val creada = llamar { cuerpoDe(api.crearObservacion(accion.datos)) }
                if (creada is ResultadoRemoto.Exito && validado) {
                    llamar { cuerpoDe(api.actualizarObservacion(creada.dato.id, ActualizarObservacionDto(texto, true))) }
                } else {
                    creada
                }
            }
            is AccionObservacion.Actualizar -> llamar { cuerpoDe(api.actualizarObservacion(accion.id, accion.datos)) }
        }
    }

    /** Los intentos que el servidor recibió de un estudiante. */
    suspend fun intentosEnServidor(estudiante: String): ResultadoRemoto<List<IntentoServidorDto>> =
        llamar { ResultadoRemoto.Exito(api.intentosDelEstudiante(estudiante)) }

    // ---------- Ayudantes internos ----------

    private fun <T> cuerpoDe(respuesta: Response<T>): ResultadoRemoto<T> {
        val cuerpo = respuesta.body()
        return if (respuesta.isSuccessful && cuerpo != null) ResultadoRemoto.Exito(cuerpo) else errorDe(respuesta)
    }

    private fun errorDe(respuesta: Response<*>): ResultadoRemoto<Nothing> =
        if (respuesta.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            sesion.borrarToken()
            ResultadoRemoto.SinSesion
        } else {
            Log.w(TAG, "El servidor respondió ${respuesta.code()}")
            ResultadoRemoto.Error("El servidor respondió ${respuesta.code()}")
        }

    /** Ejecuta la llamada en IO, sin token no llama, y convierte cualquier falla en un ResultadoRemoto. */
    private suspend fun <T> llamar(bloque: suspend () -> ResultadoRemoto<T>): ResultadoRemoto<T> =
        withContext(Dispatchers.IO) {
            if (sesion.token.value == null) return@withContext ResultadoRemoto.SinSesion
            try {
                bloque()
            } catch (e: CancellationException) {
                throw e  // la cancelación de la corrutina no es un error de red
            } catch (e: IOException) {
                Log.w(TAG, "Sin conexión con ms-resultados", e)
                ResultadoRemoto.SinConexion
            } catch (e: HttpException) {
                if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    sesion.borrarToken()
                    ResultadoRemoto.SinSesion
                } else {
                    ResultadoRemoto.Error("El servidor respondió ${e.code()}")
                }
            } catch (e: RuntimeException) {
                Log.w(TAG, "Respuesta del servidor que no se pudo leer", e)
                ResultadoRemoto.Error("Respuesta inesperada del servidor")
            }
        }

    private companion object {
        const val TAG = "ObservacionRemota"
    }
}
