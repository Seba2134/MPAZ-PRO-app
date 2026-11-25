package cl.duoc.mpazpro.ui.estudiante

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.mpazpro.ui.theme.AzulClaro
import cl.duoc.mpazpro.ui.theme.AzulPrincipal

// Piezas visuales que comparten las pantallas del estudiante.
// Solo dibujan: no tienen lógica de la app.

/** Un emoji que acompaña a cada asignatura (por su nombre). */
fun emojiDeAsignatura(nombre: String): String {
    val n = nombre.lowercase()
    return when {
        "lenguaje" in n -> "📖"
        "matem" in n -> "🔢"
        "ciencia" in n -> "🌱"
        "historia" in n -> "🗺️"
        else -> "⭐"
    }
}

/** Un mensaje de ánimo según cuánto lleva de la unidad. */
fun mensajeAvance(porcentaje: Int): String = when {
    porcentaje >= 100 -> "¡Completaste todo! 🏆"
    porcentaje >= 50 -> "¡Vas muy bien! 💪"
    porcentaje > 0 -> "¡Buen comienzo! 🌟"
    else -> "¡A empezar! 🚀"
}

/** Estrellas de la pantalla final: 3 con 75 % o más, 2 con 50 % o más, 1 si no. Es solo para animar. */
fun estrellasPorPuntaje(correctas: Int, total: Int): Int {
    if (total <= 0) return 1
    val porcentaje = correctas * 100 / total
    return when {
        porcentaje >= 75 -> 3
        porcentaje >= 50 -> 2
        else -> 1
    }
}

/** Colores para las letras de las alternativas (A, B, C, D…). */
val coloresAlternativas = listOf(
    Color(0xFF1E88E5),
    Color(0xFFE53935),
    Color(0xFF43A047),
    Color(0xFFFB8C00),
    Color(0xFF8E24AA)
)

/**
 * Encabezado de colores de las pantallas del estudiante: emoji grande, título y subtítulo.
 * Si [onVolver] no es null, muestra la flecha para volver.
 */
@Composable
fun BannerEstudiante(
    titulo: String,
    emoji: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    color: Color = AzulPrincipal,
    onVolver: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.75f))))
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 34.sp)
            }
        }
    }
}

/** Título de una sección con su emoji: «📚 Aprende». */
@Composable
fun TituloSeccion(emoji: String, texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 28.sp)
        Spacer(Modifier.width(10.dp))
        Text(texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

/** Una etiqueta redondeada de color con texto (estado de la prueba, fechas, «Hecho»…). */
@Composable
fun Pildora(texto: String, fondo: Color, colorTexto: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        color = colorTexto,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

/** Anillo de avance con el porcentaje al centro; se llena con una animación. */
@Composable
fun AnilloAvance(porcentaje: Int, modifier: Modifier = Modifier) {
    val progreso by animateFloatAsState(
        targetValue = porcentaje.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "anilloAvance"
    )
    Box(modifier = modifier.size(96.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progreso },
            modifier = Modifier.size(96.dp),
            strokeWidth = 10.dp,
            color = AzulPrincipal,
            trackColor = AzulClaro
        )
        Text("$porcentaje %", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

/** Los intentos como círculos: llenos los usados, vacíos los que quedan. */
@Composable
fun IndicadorIntentos(usados: Int, maximo: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(maximo) { i ->
                val usado = i < usados
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            if (usado) AzulPrincipal else AzulClaro,
                            CircleShape
                        )
                )
            }
        }
        Text("Intentos: $usados de $maximo", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Círculo de color con la letra de una alternativa (A, B, C…). */
@Composable
fun BurbujaLetra(letra: Char, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letra.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Un emoji grande con un mensaje, para estados vacíos o de espera. */
@Composable
fun MensajeConEmoji(emoji: String, texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 64.sp)
        Text(texto, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
