package cl.duoc.mpazpro.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Maneja los archivos de fotos y audios de los contenidos.
 * Todo se guarda en el almacenamiento INTERNO de la app (filesDir/contenidos).
 */
class ArchivosContenido(private val context: Context) {
    private val carpeta: File
        get() = File(context.filesDir, CARPETA).apply { mkdirs() }

    fun nuevaFoto(): File = File(carpeta, "img_${System.currentTimeMillis()}.jpg")
    fun nuevoAudio(): File = File(carpeta, "aud_${System.currentTimeMillis()}.m4a")

    fun uriPara(archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    fun borrar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }

    companion object {
        const val CARPETA = "contenidos"
    }
}