package cl.duoc.mpazpro.ui.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.relation.UnidadEstudiante
import cl.duoc.mpazpro.ui.theme.AmarilloClaro
import cl.duoc.mpazpro.ui.theme.RojoError
import cl.duoc.mpazpro.ui.theme.TextoOscuro
import cl.duoc.mpazpro.ui.theme.VerdeClaro
import cl.duoc.mpazpro.ui.theme.VerdeHoja
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Versión "conectada": obtiene el ViewModel con el curso del estudiante. */
@Composable
fun EstudianteInicioRoute(
    cursoId: Long?,
    nombre: String,
    onAbrirUnidad: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: EstudianteViewModel = viewModel(factory = EstudianteViewModel.factory(cursoId))
) {
    val estado by viewModel.uiState.collectAsState()

    EstudianteInicioScreen(
        nombre = nombre,
        estado = estado,
        onAbrirUnidad = onAbrirUnidad,
        onCerrarSesion = onCerrarSesion
    )
}

@Composable
fun EstudianteInicioScreen(
    nombre: String,
    estado: EstudianteUiState,
    onAbrirUnidad: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BannerEstudiante(
                titulo = "¡Hola, $nombre!",
                emoji = "👋",
                subtitulo = estado.cursoNombre.takeIf { it.isNotEmpty() }?.let { "Tu curso: $it" }
            )
        }

        when {
            estado.cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            estado.sinCurso -> item {
                MensajeConEmoji(
                    emoji = "🏫",
                    texto = "Todavía no tienes un curso asignado. Pide ayuda a tu profesora o profesor."
                )
            }

            estado.asignaturas.isEmpty() -> item {
                MensajeConEmoji(
                    emoji = "🌈",
                    texto = "Por ahora no tienes unidades por cursar. ¡Vuelve pronto!"
                )
            }

            else -> {
                item {
                    val total = estado.asignaturas.sumOf { it.unidades.size }
                    TituloSeccion(
                        emoji = "🚀",
                        texto = if (total == 1) "Tienes 1 unidad por explorar" else "Tienes $total unidades por explorar"
                    )
                }
                items(estado.asignaturas, key = { it.id }) { asignatura ->
                    TarjetaAsignatura(asignatura, onAbrirUnidad)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text("Cerrar sesión", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Una tarjeta por asignatura, con su color y su emoji. Al tocar el encabezado se abre o se cierra. */
@Composable
private fun TarjetaAsignatura(asignatura: AsignaturaPorCursar, onAbrirUnidad: (Long) -> Unit) {
    val colorAsignatura = Color(asignatura.colorHex.toColorInt())
    var abierta by rememberSaveable(asignatura.id) { mutableStateOf(true) }
    val giroFlecha by animateFloatAsState(if (abierta) 180f else 0f, label = "giroFlecha")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorAsignatura.copy(alpha = 0.10f)),
        border = BorderStroke(2.dp, colorAsignatura.copy(alpha = 0.5f))
    ) {
        Column {
            // Encabezado: emoji en un círculo de color + nombre + cantidad + flecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { abierta = !abierta }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(colorAsignatura, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emojiDeAsignatura(asignatura.nombre), fontSize = 28.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asignatura.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = textoCantidad(asignatura.unidades.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (abierta) "Ocultar unidades" else "Ver unidades",
                    modifier = Modifier.rotate(giroFlecha)
                )
            }

            // Las unidades aparecen y desaparecen con animación
            AnimatedVisibility(visible = abierta) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    asignatura.unidades.forEach { unidad ->
                        FilaUnidad(
                            unidad = unidad,
                            colorAsignatura = colorAsignatura,
                            onClick = { onAbrirUnidad(unidad.unidadId) }
                        )
                    }
                }
            }
        }
    }
}

/** Una unidad por cursar, como mini-tarjeta. Se toca para abrirla. */
@Composable
private fun FilaUnidad(unidad: UnidadEstudiante, colorAsignatura: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = unidad.unidadTitulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (unidad.instrucciones.isNotBlank()) {
                    // El mensaje del docente, como una burbuja
                    Text(
                        text = "💬 “${unidad.instrucciones}”",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = TextoOscuro,
                        modifier = Modifier
                            .background(AmarilloClaro, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                PildoraPrueba(unidad)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorAsignatura, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Abrir unidad", tint = Color.White)
            }
        }
    }
}

/** El estado de la prueba final como una etiqueta de color (siempre con texto). */
@Composable
private fun PildoraPrueba(unidad: UnidadEstudiante) {
    val (texto, situacion) = estadoPrueba(unidad)
    val (fondo, colorTexto) = when (situacion) {
        SituacionPrueba.DISPONIBLE -> VerdeClaro to VerdeHoja
        SituacionPrueba.POR_ABRIR -> AmarilloClaro to TextoOscuro
        SituacionPrueba.CERRADA -> RojoError.copy(alpha = 0.12f) to RojoError
    }
    Pildora(texto = "🏆 $texto", fondo = fondo, colorTexto = colorTexto)
}

private enum class SituacionPrueba { DISPONIBLE, POR_ABRIR, CERRADA }

private fun textoCantidad(cantidad: Int): String =
    if (cantidad == 1) "1 unidad por cursar" else "$cantidad unidades por cursar"

private val FORMATO_CORTO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun fechaCorta(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATO_CORTO)

/** El estado de la prueba final según sus fechas, en palabras simples. */
private fun estadoPrueba(
    unidad: UnidadEstudiante,
    ahora: Long = System.currentTimeMillis()
): Pair<String, SituacionPrueba> {
    val intentos = if (unidad.intentosMaxPrueba == 1) "1 intento" else "${unidad.intentosMaxPrueba} intentos"
    val inicio = unidad.fechaInicioPrueba
    val fin = unidad.fechaFinPrueba
    return when {
        inicio != null && ahora < inicio -> "Prueba final: abre el ${fechaCorta(inicio)}" to SituacionPrueba.POR_ABRIR
        fin != null && ahora > fin -> "Prueba final: cerrada" to SituacionPrueba.CERRADA
        fin != null -> "Prueba final: disponible hasta el ${fechaCorta(fin)} · $intentos" to SituacionPrueba.DISPONIBLE
        else -> "Prueba final: disponible · $intentos" to SituacionPrueba.DISPONIBLE
    }
}
