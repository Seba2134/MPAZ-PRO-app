package cl.duoc.mpazpro.ui.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.relation.RespuestaDetalle
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.ImagenArchivo
import cl.duoc.mpazpro.ui.components.MensajeError
import cl.duoc.mpazpro.ui.theme.AmarilloSol
import cl.duoc.mpazpro.ui.theme.RojoError
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = estado.titulo.ifEmpty { "Actividad" },
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        when (val fase = estado.fase) {
            Fase.Cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Fase.Bloqueada -> item { VistaBloqueada(mensaje = fase.mensaje, onVolver = onVolver) }

            Fase.Respondiendo -> preguntaEnPantalla(
                estado = estado,
                onElegir = onElegir,
                onSiguiente = onSiguiente,
                onEscucharPregunta = onEscucharPregunta
            )

            is Fase.Terminada -> {
                item { VistaTerminada(fase = fase, estado = estado) }
                if (estado.mostrarDetalle) {
                    items(estado.detalle, key = { it.preguntaId }) { FilaDetalle(it) }
                }
                item { BotonGrande(texto = "Volver a la unidad", onClick = onVolver) }
            }
        }
    }
}

/** Una sola pregunta, con sus alternativas y (en los desafíos) la retroalimentación. */
private fun androidx.compose.foundation.lazy.LazyListScope.preguntaEnPantalla(
    estado: RendirActividadUiState,
    onElegir: (Long) -> Unit,
    onSiguiente: () -> Unit,
    onEscucharPregunta: () -> Unit
) {
    val actual = estado.preguntaActual ?: return
    val pregunta = actual.pregunta
    val elegida = estado.opcionElegidaActual
    val acierto = estado.aciertos[pregunta.id]

    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (estado.instruccion.isNotBlank()) {
                Text(estado.instruccion, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                text = "Pregunta ${estado.indiceActual + 1} de ${estado.preguntas.size}",
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = { (estado.indiceActual + 1) / estado.preguntas.size.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    item {
        Text(pregunta.enunciado, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }

    pregunta.recursoImagen?.let { ruta ->
        item { ImagenArchivo(ruta = ruta, descripcion = "Imagen de la pregunta") }
    }

    pregunta.recursoAudio?.let { ruta ->
        item {
            val existe = remember(ruta) { File(ruta).exists() }
            if (existe) {
                OutlinedButton(
                    onClick = onEscucharPregunta,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    Icon(
                        imageVector = if (estado.reproduciendoAudio) Icons.Filled.Close else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Text(
                        text = if (estado.reproduciendoAudio) "  Detener" else "  Escuchar pregunta",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    // Todos los tipos de pregunta se muestran como selección (los otros tipos están fuera de alcance)
    items(actual.opciones, key = { it.id }) { opcion ->
        val seleccionada = elegida == opcion.id
        OutlinedButton(
            onClick = { onElegir(opcion.id) },
            enabled = elegida == null && !estado.guardando,
            border = if (seleccionada) {
                BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.outlinedButtonBorder(enabled = elegida == null)
            },
            colors = if (seleccionada) {
                ButtonDefaults.outlinedButtonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                ButtonDefaults.outlinedButtonColors()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
        ) {
            if (seleccionada) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Elegida")
            }
            Text(
                text = opcion.texto,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    // Retroalimentación inmediata: solo en los desafíos
    item {
        AnimatedVisibility(visible = !estado.esPruebaFinal && acierto != null) {
            Retroalimentacion(
                acierto = acierto == true,
                correcta = actual.opciones.firstOrNull { it.esCorrecta }?.texto
            )
        }
    }

    estado.error?.let { mensaje ->
        item { MensajeError(mensaje) }
    }

    item {
        AnimatedVisibility(visible = elegida != null) {
            BotonGrande(
                texto = if (estado.esUltima) "Terminar" else "Siguiente",
                onClick = onSiguiente,
                cargando = estado.guardando
            )
        }
    }
}

@Composable
private fun Retroalimentacion(acierto: Boolean, correcta: String?) {
    val color = if (acierto) VerdeHoja else RojoError
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (acierto) Icons.Filled.CheckCircle else Icons.Filled.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = if (acierto) "¡Bien hecho!" else "Casi. La respuesta era: ${correcta.orEmpty()}",
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VistaTerminada(fase: Fase.Terminada, estado: RendirActividadUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = AmarilloSol, modifier = Modifier.size(72.dp))
        Text("¡Terminaste!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            text = "Respondiste bien ${fase.correctas} de ${fase.total}",
            style = MaterialTheme.typography.titleLarge
        )
        if (!estado.mostrarDetalle) {
            Text(
                text = "Tu profesor revisará tus respuestas",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Una pregunta del resultado: ✓/✗, lo que eligió y la respuesta correcta. */
@Composable
private fun FilaDetalle(detalle: RespuestaDetalle) {
    val color = if (detalle.esCorrecta) VerdeHoja else RojoError
    OutlinedCard(modifier = Modifier.fillMaxWidth(), border = BorderStroke(2.dp, color)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (detalle.esCorrecta) Icons.Filled.CheckCircle else Icons.Filled.Close,
                contentDescription = if (detalle.esCorrecta) "Correcta" else "Incorrecta",
                tint = color
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(detalle.enunciado, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Elegiste: ${detalle.respuestaElegida ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                if (!detalle.esCorrecta) {
                    Text(
                        "La correcta era: ${detalle.respuestaCorrecta ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun VistaBloqueada(mensaje: String, onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Text(mensaje, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        BotonGrande(texto = "Volver", onClick = onVolver)
    }
}
