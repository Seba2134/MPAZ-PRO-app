package cl.duoc.mpazpro.util

import android.content.Context
import android.media.MediaPlayer

class AudioPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun reproducirAudio(resId: Int) {
        detenerAudio()
        mediaPlayer = MediaPlayer.create(context, resId)?.apply {
            setOnCompletionListener { detenerAudio() }
            start()
        }
    }

    fun detenerAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}