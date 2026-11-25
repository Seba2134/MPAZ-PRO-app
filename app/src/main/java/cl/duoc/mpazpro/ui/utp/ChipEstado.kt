package cl.duoc.mpazpro.ui.utp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.ui.theme.AmarilloSol
import cl.duoc.mpazpro.ui.theme.AzulPrincipal
import cl.duoc.mpazpro.ui.theme.RojoError
import cl.duoc.mpazpro.ui.theme.TextoOscuro
import cl.duoc.mpazpro.ui.theme.VerdeHoja

/** El estado de una unidad como una etiqueta de color, siempre con su nombre (el color nunca va solo). */
@Composable
fun ChipEstado(estado: EstadoContenido, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (estado) {
        EstadoContenido.BORRADOR -> Color(0xFF757575) to Color.White
        EstadoContenido.EN_REVISION -> AmarilloSol to TextoOscuro
        EstadoContenido.AUTORIZADO -> AzulPrincipal to Color.White
        EstadoContenido.PUBLICADO -> VerdeHoja to Color.White
        EstadoContenido.DESHABILITADO -> RojoError to Color.White
    }
    Text(
        text = estado.nombreVisible,
        color = texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

/** Cómo se llama, en un botón, el paso de la unidad a [nuevo]. */
fun etiquetaCambio(nuevo: EstadoContenido): String = when (nuevo) {
    EstadoContenido.BORRADOR -> "Volver a borrador"
    EstadoContenido.EN_REVISION -> "Enviar a revisión"
    EstadoContenido.AUTORIZADO -> "Autorizar"
    EstadoContenido.PUBLICADO -> "Publicar"
    EstadoContenido.DESHABILITADO -> "Deshabilitar"
}
