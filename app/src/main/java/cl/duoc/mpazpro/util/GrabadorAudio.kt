package cl.duoc.mpazpro.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class GrabadorAudio(private val context: Context) {
    private var grabador: MediaRecorder? = null
    private var archivoActual: File? = null

    val estaGrabando: Boolean
        get() = grabador != null

    fun iniciar(archivo: File) {
        cancelar()
        val nuevo = crearGrabador().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64_000)
            setAudioSamplingRate(44_100)
            setOutputFile(archivo.absolutePath)
        }
        try {
            nuevo.prepare()
            nuevo.start()
        } catch (e: Exception) {
            nuevo.release()
            archivo.delete()
            throw e
        }
        grabador = nuevo
        archivoActual = archivo
    }

    fun detener(): File? {
        val actual = grabador ?: return null
        val archivo = archivoActual
        grabador = null
        archivoActual = null
        return try {
            actual.stop()
            archivo
        } catch (e: RuntimeException) {
            archivo?.delete()
            null
        } finally {
            actual.release()
        }
    }

    fun cancelar() {
        detener()?.delete()
    }

    private fun crearGrabador(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
}