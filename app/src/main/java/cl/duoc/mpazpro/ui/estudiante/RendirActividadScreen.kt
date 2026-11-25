package cl.duoc.mpazpro.ui.estudiante

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.relation.RespuestaDetalle
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.ImagenArchivo
import cl.duoc.mpazpro.ui.components.MensajeError
import cl.duoc.mpazpro.ui.theme.AmarilloSol
import cl.duoc.mpazpro.ui.theme.AzulClaro
import cl.duoc.mpazpro.ui.theme.AzulPrincipal
import cl.duoc.mpazpro.ui.theme.RojoError
import cl.duoc.mpazpro.ui.theme.TextoOscuro
import cl.duoc.mpazpro.ui.theme.VerdeClaro
import cl.duoc.mpazpro.ui.theme.VerdeHoja
import java.io.File

/** Versión "conectada": obtiene el ViewModel de esta actividad. */
@Composable
fun RendirActividadRoute(
    actividadId: Long,
    onVolver: () -> Unit,
    viewModel: RendirActividadViewModel = viewModel(factory = RendirActividadViewModel.factory(actividadId))
) {
    val estado by viewModel.uiState.collectAsState()

    RendirActividadScreen(
        estado = estado,
        onElegir = viewModel::elegir,
        onSiguiente = viewModel::siguiente,
        onEscucharPregunta = viewModel::alternarAudioPregunta,
        onVolver = onVolver
    )
}

@Composable
fun RendirActividadScreen(
    estado: RendirActividadUiState,
    onElegir: (Long) -> Unit,
    onSiguiente: () -> Unit,
    onEscucharPregunta: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- Encabezado ----------
        item {
            BannerEstudiante(
                titulo = estado.titulo.ifEmpty { "Actividad" },
                emoji = if (estado.esPruebaFinal) "🏆" else "🎯",
                subtitulo = if (estado.esPruebaFinal) "Prueba final" else "Desafío",
                color = if (estado.esPruebaFinal) AzulPrincipal else VerdeHoja,
                onVolver = onVolver
            )
        }

        when (val fase = estado.fase) {
            Fase.Cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Fase.Bloqueada -> item { VistaBloqueada(mensaje = fase.mensaje, onVolver = onVolver) }

            Fase.Respondiendo -> item {
                // Cada pregunta entra deslizándose desde la derecha
                AnimatedContent(
                    targetState = estado.indiceActual,
                    transitionSpec = {
                        (slideInHorizontally { ancho -> ancho } + fadeIn()) togetherWith
                            (slideOutHorizontally { ancho -> -ancho } + fadeOut())
                    },
                    label = "pregunta"
                ) { indice ->
                    PreguntaEnPantalla(
                        estado = estado,
                        indice = indice,
                        onElegir = onElegir,
                        onSiguiente = onSiguiente,
                        onEscucharPregunta = onEscucharPregunta
                    )
                }
            }

            is Fase.Terminada -> {
                item { VistaTerminada(fase = fase, estado = estado) }
                if (estado.mostrarDetalle) {
                    items(estado.detalle, key = { "respuesta-${it.preguntaId}" }) { FilaDetalle(it) }
                }
                item { BotonGrande(texto = "Volver a la unidad", onClick = onVolver) }
            }
        }
    }
}

/** Una sola pregunta, con sus alternativas y (en los desafíos) la retroalimentación. */
@Composable
private fun PreguntaEnPantalla(
    estado: RendirActividadUiState,
    indice: Int,
    onElegir: (Long) -> Unit,
    onSiguiente: () -> Unit,
    onEscucharPregunta: () -> Unit
) {
    val actual = estado.preguntas.getOrNull(indice) ?: return
    val pregunta = actual.pregunta
    val elegida = estado.respuestas[pregunta.id]
    val acierto = estado.aciertos[pregunta.id]
    val esUltima = indice == estado.preguntas.lastIndex
    val progreso by animateFloatAsState(
        targetValue = (indice + 1) / estado.preguntas.size.toFloat(),
        label = "progresoPreguntas"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (estado.instruccion.isNotBlank()) {
            Text("👉 ${estado.instruccion}", style = MaterialTheme.typography.bodyLarge)
        }

        // ---------- Progreso ----------
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Pregunta ${indice + 1} de ${estado.preguntas.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(50)),
                color = AmarilloSol,
                trackColor = AzulClaro,
                drawStopIndicator = {}
            )
        }

        // ---------- Enunciado ----------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AzulClaro.copy(alpha = 0.5f), contentColor = TextoOscuro)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(pregunta.enunciado, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                pregunta.recursoImagen?.let { ruta ->
                    ImagenArchivo(ruta = ruta, descripcion = "Imagen de la pregunta")
                }
                pregunta.recursoAudio?.let { ruta ->
                    val existe = remember(ruta) { File(ruta).exists() }
                    if (existe) {
                        Button(
                            onClick = onEscucharPregunta,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (estado.reproduciendoAudio) RojoError else VerdeHoja
                            ),
                            modifier = Modifier.heightIn(min = 56.dp)
                        ) {
                            Icon(
                                imageVector = if (estado.reproduciendoAudio) Icons.Filled.Close else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                            Text(
                                text = if (estado.reproduciendoAudio) "  Detener" else "  🔊 Escuchar pregunta",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // ---------- Alternativas ----------
        // Todos los tipos de pregunta se muestran como selección (los otros tipos están fuera de alcance)
        actual.opciones.forEachIndexed { i, opcion ->
            TarjetaAlternativa(
                opcion = opcion,
                letra = 'A' + i,
                color = coloresAlternativas[i % coloresAlternativas.size],
                elegida = elegida,
                esPruebaFinal = estado.esPruebaFinal,
                habilitada = elegida == null && !estado.guardando,
                onClick = { onElegir(opcion.id) }
            )
        }

        // Retroalimentación inmediata: solo en los desafíos
        AnimatedVisibility(
            visible = !estado.esPruebaFinal && acierto != null,
            enter = scaleIn() + fadeIn()
        ) {
            Retroalimentacion(
                acierto = acierto == true,
                correcta = actual.opciones.firstOrNull { it.esCorrecta }?.texto
            )
        }

        estado.error?.let { mensaje -> MensajeError(mensaje) }

        AnimatedVisibility(visible = elegida != null, enter = fadeIn()) {
            BotonGrande(
                texto = if (esUltima) "Terminar" else "Siguiente",
                onClick = onSiguiente,
                cargando = estado.guardando
            )
        }
    }
}

/**
 * Una alternativa con su letra de color. Después de responder:
 * - en un desafío se pinta la correcta en verde y, si se equivocó, la elegida en rojo;
 * - en la prueba final solo se marca la elegida (no se revela si acertó).
 */
@Composable
private fun TarjetaAlternativa(
    opcion: OpcionEntity,
    letra: Char,
    color: Color,
    elegida: Long?,
    esPruebaFinal: Boolean,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val respondida = elegida != null
    val esLaElegida = elegida == opcion.id

    // Cómo se ve según lo que pasó
    val apariencia = when {
        !respondida -> Apariencia(Color.White, color.copy(alpha = 0.5f))
        esPruebaFinal && esLaElegida ->
            Apariencia(AzulClaro, AzulPrincipal, Icons.Filled.CheckCircle, "Tu respuesta", AzulPrincipal)
        esPruebaFinal -> APAGADA
        opcion.esCorrecta -> Apariencia(VerdeClaro, VerdeHoja, Icons.Filled.CheckCircle, "Correcta", VerdeHoja)
        esLaElegida ->
            Apariencia(RojoError.copy(alpha = 0.12f), RojoError, Icons.Filled.Close, "Tu respuesta, incorrecta", RojoError)
        else -> APAGADA
    }
    val (fondo, borde, icono, descripcion, colorIcono) = apariencia
    val apagada = respondida && icono == null

    Card(
        onClick = onClick,
        enabled = habilitada,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondo,
            contentColor = TextoOscuro,
            disabledContainerColor = fondo,
            disabledContentColor = if (apagada) TextoOscuro.copy(alpha = 0.45f) else TextoOscuro
        ),
        border = BorderStroke(if (icono != null) 3.dp else 2.dp, borde),
        elevation = CardDefaults.cardElevation(defaultElevation = if (respondida) 0.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BurbujaLetra(letra = letra, color = if (apagada) Color.LightGray else color)
            Text(
                text = opcion.texto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (icono != null) {
                Icon(icono, contentDescription = descripcion, tint = colorIcono, modifier = Modifier.size(32.dp))
            }
        }
    }
}

/** Los colores e ícono de una alternativa. */
private data class Apariencia(
    val fondo: Color,
    val borde: Color,
    val icono: ImageVector? = null,
    val descripcion: String? = null,
    val colorIcono: Color = Color.Unspecified
)

/** Las alternativas que no se eligieron (y no hay que destacar) quedan atenuadas. */
private val APAGADA = Apariencia(Color.White.copy(alpha = 0.6f), Color.LightGray)

@Composable
private fun Retroalimentacion(acierto: Boolean, correcta: String?) {
    val color = if (acierto) VerdeHoja else RojoError
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (acierto) VerdeClaro else RojoError.copy(alpha = 0.12f),
            contentColor = color
        ),
        border = BorderStroke(2.dp, color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (acierto) "🎉" else "💪", fontSize = 40.sp)
                Icon(
                    imageVector = if (acierto) Icons.Filled.CheckCircle else Icons.Filled.Close,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = if (acierto) "¡Bien hecho!" else "Casi. La respuesta era: ${correcta.orEmpty()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VistaTerminada(fase: Fase.Terminada, estado: RendirActividadUiState) {
    val estrellas = estrellasPorPuntaje(fase.correctas, fase.total)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AzulClaro.copy(alpha = 0.5f), contentColor = TextoOscuro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Las estrellas se llenan según el puntaje (solo para animar, no es una nota)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i < estrellas) AmarilloSol else Color.LightGray,
                        modifier = Modifier.size(if (i == 1) 84.dp else 64.dp)
                    )
                }
            }
            Text("¡Terminaste! 🎉", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(AzulPrincipal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${fase.correctas}/${fase.total}",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Respondiste bien ${fase.correctas} de ${fase.total}",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            if (!estado.mostrarDetalle) {
                Text(
                    text = "Tu profesor revisará tus respuestas 📝",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    if (estado.mostrarDetalle && estado.detalle.isNotEmpty()) {
        TituloSeccion("📋", "Tus respuestas", modifier = Modifier.padding(top = 16.dp))
    }
}

/** Una pregunta del resultado: ✓/✗, lo que eligió y la respuesta correcta. */
@Composable
private fun FilaDetalle(detalle: RespuestaDetalle) {
    val color = if (detalle.esCorrecta) VerdeHoja else RojoError
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (detalle.esCorrecta) VerdeClaro else RojoError.copy(alpha = 0.10f),
            contentColor = TextoOscuro
        ),
        border = BorderStroke(2.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (detalle.esCorrecta) Icons.Filled.CheckCircle else Icons.Filled.Close,
                contentDescription = if (detalle.esCorrecta) "Correcta" else "Incorrecta",
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(detalle.enunciado, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Elegiste: ${detalle.respuestaElegida ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                if (!detalle.esCorrecta) {
                    Text(
                        "La correcta era: ${detalle.respuestaCorrecta ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun VistaBloqueada(mensaje: String, onVolver: () -> Unit) {
    val emoji = when {
        "abre" in mensaje || "cerró" in mensaje -> "⏰"
        "intentos" in mensaje -> "✋"
        else -> "🔒"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AzulClaro.copy(alpha = 0.5f), contentColor = TextoOscuro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(emoji, fontSize = 72.sp)
            Text(mensaje, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            BotonGrande(texto = "Volver", onClick = onVolver)
        }
    }
}
