package cl.duoc.mpazpro.util

import android.content.Context
import android.media.MediaPlayer

class AudioPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    val estaReproduciendo: Boolean
        get() = mediaPlayer?.isPlaying == true

    fun reproducirAudio(resId: Int) {
        detenerAudio()
        mediaPlayer = MediaPlayer.create(context, resId)?.apply {
            setOnCompletionListener { detenerAudio() }
            start()
        }
    }

    fun reproducirArchivo(ruta: String, alTerminar: () -> Unit = {}) {
        detenerAudio()
        val reproductor = MediaPlayer()
        try {
            reproductor.setDataSource(ruta)
            reproductor.setOnCompletionListener {
                detenerAudio()
                alTerminar()
            }
            reproductor.prepare()
            reproductor.start()
            mediaPlayer = reproductor
        } catch (e: Exception) {
            reproductor.release()
            alTerminar()
        }
    }

    fun detenerAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            mediaPlayer = null
        }
    }
}