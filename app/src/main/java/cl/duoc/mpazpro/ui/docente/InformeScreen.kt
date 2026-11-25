package cl.duoc.mpazpro.ui.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.relation.RendimientoOa
import cl.duoc.mpazpro.data.local.relation.RespuestaDetalle
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.components.Encabezado
import cl.duoc.mpazpro.ui.components.TarjetaMensaje
import cl.duoc.mpazpro.ui.theme.RojoError
import cl.duoc.mpazpro.ui.theme.VerdeHoja
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun InformeRoute(
    asignacionId: Long,
    estudianteId: Long,
    onVolver: () -> Unit,
    viewModel: InformeViewModel = viewModel(
        key = "informe-$asignacionId-$estudianteId",
        factory = InformeViewModel.factory(asignacionId, estudianteId)
    )
) {
    val estado by viewModel.uiState.collectAsState()

    InformeScreen(
        estado = estado,
        onObservacionChange = viewModel::onObservacionChange,
        onGuardarObservacion = viewModel::guardarObservacion,
        onValidarInforme = viewModel::validarInforme,
        onMensajeVisto = viewModel::mensajeVisto,
        onVolver = onVolver
    )
}

@Composable
fun InformeScreen(
    estado: InformeUiState,
    onObservacionChange: (String) -> Unit,
    onGuardarObservacion: () -> Unit,
    onValidarInforme: () -> Unit,
    onMensajeVisto: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Encabezado(titulo = "Informe", onVolver = onVolver) }

        if (estado.error != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("  ${estado.error}", style = MaterialTheme.typography.bodyLarge)
                }
            }
            return@LazyColumn
        }

        if (estado.cargando) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            return@LazyColumn
        }

        // ---------- Encabezado del informe ----------
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(estado.estudiante, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${estado.asignatura} · ${estado.unidad}", style = MaterialTheme.typography.bodyMedium)
                estado.mejorIntento?.fechaFin?.let {
                    Text("Mejor intento: ${fechaHora(it)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        val mejor = estado.mejorIntento
        if (mejor == null) {
            item { Text("Todavía no rinde la prueba final", style = MaterialTheme.typography.bodyLarge) }
            return@LazyColumn
        }

        // ---------- Puntaje global ----------
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Puntaje global", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${estado.correctasTotales} de ${estado.preguntasTotales} (${estado.porcentajeGlobal} %)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { estado.porcentajeGlobal / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ---------- Rendimiento por OA ----------
        item { Text("Por objetivo de aprendizaje", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Text(
                text = "Etiqueta según una regla fija (≥ 75 % Fortaleza, 50–74 % En desarrollo, < 50 % Reforzar). Es una guía para el docente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(estado.rendimiento, key = { "oa-${it.oaId}" }) { TarjetaOa(it) }

        // ---------- Desglose de respuestas ----------
        item { Text("Respuestas del mejor intento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(estado.detalle, key = { "respuesta-${it.preguntaId}" }) { FilaRespuesta(it) }

        // ---------- Observación ----------
        item { Text("Observación del docente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { TarjetaMensaje(mensaje = estado.mensaje, onVisto = onMensajeVisto) }
        item {
            CampoTexto(
                valor = estado.textoObservacion,
                onValorChange = onObservacionChange,
                etiqueta = "Observación",
                error = estado.errorObservacion,
                multilinea = true,
                enabled = !estado.guardando
            )
        }
        item {
            BotonGrande(
                texto = "Guardar observación",
                onClick = onGuardarObservacion,
                cargando = estado.guardando
            )
        }
        item {
            val guardada = estado.observacionGuardada
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (guardada?.validado == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeHoja)
                        Text(" Informe validado ✓", color = VerdeHoja, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Si guardas otra observación, tendrás que validar el informe de nuevo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedButton(
                        onClick = onValidarInforme,
                        enabled = guardada != null && !estado.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Validar informe")
                    }
                    if (guardada == null) {
                        Text(
                            text = "Guarda una observación para poder validar el informe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOa(oa: RendimientoOa) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = oa.codigo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(oa.etiqueta, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(oa.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text("${oa.correctas} de ${oa.total} (${oa.porcentaje} %)", style = MaterialTheme.typography.bodyLarge)
            LinearProgressIndicator(progress = { oa.porcentaje / 100f }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun FilaRespuesta(detalle: RespuestaDetalle) {
    val color = if (detalle.esCorrecta) VerdeHoja else RojoError
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            imageVector = if (detalle.esCorrecta) Icons.Filled.CheckCircle else Icons.Filled.Close,
            contentDescription = if (detalle.esCorrecta) "Correcta" else "Incorrecta",
            tint = color
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${detalle.oaCodigo} · ${detalle.enunciado}", style = MaterialTheme.typography.bodyLarge)
            Text("Eligió: ${detalle.respuestaElegida ?: "—"}", style = MaterialTheme.typography.bodyMedium)
            Text("Correcta: ${detalle.respuestaCorrecta ?: "—"}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val FORMATO_FECHA_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun fechaHora(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(FORMATO_FECHA_HORA)
