package cl.duoc.mpazpro.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Mientras se lee el archivo, cuando ya está lista, o cuando no se pudo leer. */
private sealed interface EstadoImagen {
    data object Cargando : EstadoImagen
    data class Lista(val imagen: ImageBitmap) : EstadoImagen
    data object NoDisponible : EstadoImagen
}

/**
 * Muestra una foto guardada en el almacenamiento interno.
 * La lee reducida y en segundo plano, para no trabar la pantalla.
 * Si el archivo no existe o no se puede leer, muestra una caja gris con un aviso (nunca se cae).
 */
@Composable
fun ImagenArchivo(
    ruta: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val estado by produceState<EstadoImagen>(initialValue = EstadoImagen.Cargando, ruta) {
        val imagen = withContext(Dispatchers.IO) { cargarMiniatura(ruta, ladoMaximo = 1024) }
        value = if (imagen != null) EstadoImagen.Lista(imagen) else EstadoImagen.NoDisponible
    }

    val forma = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .clip(RoundedCornerShape(16.dp))

    when (val actual = estado) {
        is EstadoImagen.Lista -> Image(
            bitmap = actual.imagen,
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifier.then(forma)
        )

        EstadoImagen.Cargando -> Box(
            modifier = modifier
                .then(forma)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        EstadoImagen.NoDisponible -> Box(
            modifier = modifier
                .then(forma)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Imagen no disponible",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Lee la imagen reducida para que su lado más largo no pase de [ladoMaximo] píxeles. */
private fun cargarMiniatura(ruta: String, ladoMaximo: Int): ImageBitmap? = try {
    if (!File(ruta).exists()) {
        null
    } else {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, medidas)
        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) {
            null
        } else {
            var escala = 1
            while (medidas.outWidth / escala > ladoMaximo || medidas.outHeight / escala > ladoMaximo) {
                escala *= 2
            }
            val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
            BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
        }
    }
} catch (e: Exception) {
    null
}
