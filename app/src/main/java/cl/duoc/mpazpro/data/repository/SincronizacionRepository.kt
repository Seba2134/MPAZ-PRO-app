package cl.duoc.mpazpro.data.repository

import android.util.Log
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.dao.IntentoDao
import cl.duoc.mpazpro.data.local.dao.RespuestaDao
import cl.duoc.mpazpro.data.local.relation.IntentoParaEnviar
import cl.duoc.mpazpro.data.local.relation.RespuestaParaEnviar
import cl.duoc.mpazpro.data.remote.MpazApi
import cl.duoc.mpazpro.data.remote.dto.IntentoDto
import cl.duoc.mpazpro.data.remote.dto.LoginDto
import cl.duoc.mpazpro.data.remote.dto.RespuestaDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

/** Qué pasó al intentar enviar los resultados pendientes. */
sealed class ResultadoSync {
    /** Se enviaron [cantidad] resultados (puede ser 0 si no había pendientes). */
    data class Enviados(val cantidad: Int) : ResultadoSync()

    /** El servidor no respondió (apagado o sin red). Los resultados siguen pendientes. */
    data object SinConexion : ResultadoSync()

    /** No hay token del servidor (no se pudo iniciar sesión allá, o venció). */
    data object SinSesion : ResultadoSync()
}

/**
 * Envía al servidor una COPIA de los intentos terminados. Room sigue siendo la fuente de verdad:
 * si el servidor está apagado, todo queda pendiente y la app funciona igual.
 * Ninguna función lanza errores hacia la pantalla.
 */
class SincronizacionRepository(
    private val api: MpazApi,
    private val sesion: Sesion,
    private val intentoDao: IntentoDao,
    private val respuestaDao: RespuestaDao
) {

    // Evita que dos sincronizaciones al mismo tiempo envíen lo mismo (igual el servidor no duplicaría)
    private val enviando = Mutex()

    /** Cuántos resultados terminados faltan por enviar. */
    fun pendientes(): Flow<Int> = intentoDao.contarPendientes()

    /**
     * Pide un JWT al servidor con el mismo usuario y clave del login local.
     * La clave solo se usa en esta llamada: nunca se guarda.
     */
    suspend fun iniciarSesionServidor(usuario: String, clave: String): Boolean = withContext(Dispatchers.IO) {
        val nombreUsuario = usuario.trim().lowercase()
        try {
            val respuesta = api.login(LoginDto(nombreUsuario, clave))
            // Si mientras tanto cerró sesión (o entró otra persona), el token ya no sirve
            if (sesion.usuarioActual.value?.nombreUsuario != nombreUsuario) return@withContext false
            sesion.guardarToken(respuesta.token)
            true
        } catch (e: CancellationException) {
            throw e  // la cancelación de la corrutina no es un error de red
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo conectar para iniciar sesión en el servidor", e)
            false
        } catch (e: HttpException) {
            Log.w(TAG, "El servidor rechazó el inicio de sesión: ${e.code()}")
            false
        } catch (e: RuntimeException) {
            Log.w(TAG, "Respuesta del login que no se pudo leer", e)
            false
        }
    }

    /** ¿El servidor está encendido y responde? */
    suspend fun servidorDisponible(): Boolean = withContext(Dispatchers.IO) {
        try {
            api.salud()["estado"] == "ok"
        } catch (e: CancellationException) {
            throw e  // la cancelación de la corrutina no es un error de red
        } catch (e: IOException) {
            false
        } catch (e: HttpException) {
            false
        } catch (e: RuntimeException) {
            false
        }
    }

    /** Envía todos los intentos pendientes. Con 200 o 201 queda marcado como sincronizado. */
    suspend fun sincronizarPendientes(): ResultadoSync = withContext(Dispatchers.IO) {
        if (sesion.token.value == null) return@withContext ResultadoSync.SinSesion

        enviando.withLock {
            var enviados = 0
            try {
                for (intento in intentoDao.obtenerPendientes()) {
                    val dto = aDto(intento, respuestaDao.obtenerParaEnviar(intento.intentoId))
                    val respuesta = api.enviarIntento(dto)
                    when {
                        respuesta.code() == HttpURLConnection.HTTP_OK ||
                            respuesta.code() == HttpURLConnection.HTTP_CREATED -> {
                            intentoDao.marcarSincronizado(intento.intentoId)
                            enviados++
                        }
                        respuesta.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            // Token vencido: se borra y lo pendiente espera al próximo inicio de sesión
                            sesion.borrarToken()
                            return@withLock ResultadoSync.SinSesion
                        }
                        // 400 o 403 (por ejemplo, el intento de otro estudiante en un equipo compartido):
                        // queda pendiente para que lo envíe un docente
                        else -> Log.w(TAG, "El servidor no aceptó el intento ${intento.intentoId}: ${respuesta.code()}")
                    }
                }
                ResultadoSync.Enviados(enviados)
            } catch (e: CancellationException) {
                throw e  // la cancelación de la corrutina no es un error de red
            } catch (e: IOException) {
                Log.w(TAG, "Sin conexión al enviar resultados", e)
                if (enviados > 0) ResultadoSync.Enviados(enviados) else ResultadoSync.SinConexion
            } catch (e: HttpException) {
                Log.w(TAG, "Error HTTP al enviar resultados: ${e.code()}")
                if (enviados > 0) ResultadoSync.Enviados(enviados) else ResultadoSync.SinConexion
            } catch (e: RuntimeException) {
                Log.w(TAG, "Error inesperado al enviar resultados", e)
                if (enviados > 0) ResultadoSync.Enviados(enviados) else ResultadoSync.SinConexion
            }
        }
    }

    companion object {
        private const val TAG = "Sincronizacion"

        /** Arma lo que se envía al servidor. Es una función pura: se prueba sin Android ni red. */
        fun aDto(intento: IntentoParaEnviar, respuestas: List<RespuestaParaEnviar>): IntentoDto = IntentoDto(
            estudianteNombreUsuario = intento.estudianteNombreUsuario,
            estudianteNombre = intento.estudianteNombre,
            cursoNombre = intento.cursoNombre,
            asignaturaNombre = intento.asignaturaNombre,
            unidadTitulo = intento.unidadTitulo,
            actividadId = intento.actividadId,
            actividadTitulo = intento.actividadTitulo,
            tipo = intento.tipo.name,
            numeroIntento = intento.numeroIntento,
            fechaInicio = intento.fechaInicio,
            fechaFin = intento.fechaFin,
            correctas = intento.correctas,
            total = intento.totalPreguntas,
            respuestas = respuestas.map {
                RespuestaDto(
                    preguntaId = it.preguntaId,
                    enunciado = it.enunciado,
                    oaCodigo = it.oaCodigo,
                    oaDescripcion = it.oaDescripcion,
                    esCorrecta = it.esCorrecta
                )
            }
        )
    }
}
