package cl.duoc.mpazpro.ui.utp

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.relation.ActividadResumen
import cl.duoc.mpazpro.data.local.relation.HistorialConUsuario
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.domain.model.TipoActividad
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.components.Encabezado
import cl.duoc.mpazpro.ui.components.TarjetaMensaje
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun UnidadDetalleRoute(
    unidadId: Long,
    onVolver: () -> Unit,
    onAbrirActividad: (Long) -> Unit,
    viewModel: UnidadDetalleViewModel = viewModel(factory = UnidadDetalleViewModel.factory(unidadId))
) {
    val estado by viewModel.uiState.collectAsState()

    UnidadDetalleScreen(
        estado = estado,
        acciones = AccionesUnidadDetalle(
            onCodigoOaChange = viewModel::onCodigoOaChange,
            onDescripcionOaChange = viewModel::onDescripcionOaChange,
            onAgregarOa = viewModel::agregarOa,
            onTituloActividadChange = viewModel::onTituloActividadChange,
            onInstruccionActividadChange = viewModel::onInstruccionActividadChange,
            onTipoActividadChange = viewModel::onTipoActividadChange,
            onCrearActividad = viewModel::crearActividad,
            onComentarioChange = viewModel::onComentarioChange,
            onSolicitarCambio = viewModel::solicitarCambio,
            onConfirmarCambio = viewModel::confirmarCambio,
            onCancelarCambio = viewModel::cancelarCambio,
            onMensajeVisto = viewModel::mensajeVisto,
            onAbrirActividad = onAbrirActividad,
            onVolver = onVolver
        )
    )
}

/** Todo lo que la pantalla de detalle puede pedirle al ViewModel (son muchas acciones). */
data class AccionesUnidadDetalle(
    val onCodigoOaChange: (String) -> Unit,
    val onDescripcionOaChange: (String) -> Unit,
    val onAgregarOa: () -> Unit,
    val onTituloActividadChange: (String) -> Unit,
    val onInstruccionActividadChange: (String) -> Unit,
    val onTipoActividadChange: (TipoActividad) -> Unit,
    val onCrearActividad: () -> Unit,
    val onComentarioChange: (String) -> Unit,
    val onSolicitarCambio: (EstadoContenido) -> Unit,
    val onConfirmarCambio: () -> Unit,
    val onCancelarCambio: () -> Unit,
    val onMensajeVisto: () -> Unit,
    val onAbrirActividad: (Long) -> Unit,
    val onVolver: () -> Unit
)

@Composable
fun UnidadDetalleScreen(
    estado: UnidadDetalleUiState,
    acciones: AccionesUnidadDetalle,
    modifier: Modifier = Modifier
) {
    val unidad = estado.unidad

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Encabezado(titulo = unidad?.titulo ?: "Unidad", onVolver = acciones.onVolver) }

        if (estado.cargando) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            return@LazyColumn
        }
        if (unidad == null) {
            item { Text("No se encontró esta unidad.", style = MaterialTheme.typography.bodyLarge) }
            return@LazyColumn
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estado actual: ", style = MaterialTheme.typography.bodyLarge)
                ChipEstado(unidad.estado)
            }
        }

        item { TarjetaMensaje(mensaje = estado.mensaje, onVisto = acciones.onMensajeVisto) }

        if (!estado.enBorrador) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("  Para editar, vuelve la unidad a Borrador", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ---------- Objetivos de aprendizaje ----------
        item { TituloSeccion("Objetivos de aprendizaje") }
        if (estado.objetivos.isEmpty()) {
            item { Text("Todavía no tiene OA.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(estado.objetivos, key = { "oa-${it.id}" }) { oa ->
            Column {
                Text(oa.codigo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(oa.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (estado.enBorrador) {
            item { FormularioOa(estado = estado, acciones = acciones) }
        }

        // ---------- Actividades ----------
        item { TituloSeccion("Actividades") }
        if (estado.actividades.isEmpty()) {
            item { Text("Todavía no tiene actividades.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(estado.actividades, key = { "actividad-${it.id}" }) { actividad ->
            FilaActividad(actividad = actividad, onClick = { acciones.onAbrirActividad(actividad.id) })
        }
        if (estado.enBorrador) {
            item { FormularioActividad(estado = estado, acciones = acciones) }
        }

        // ---------- Cambiar estado ----------
        item { TituloSeccion("Cambiar estado") }
        item {
            CampoTexto(
                valor = estado.comentario,
                onValorChange = acciones.onComentarioChange,
                etiqueta = "Comentario (opcional)",
                error = estado.errorComentario,
                enabled = !estado.guardando
            )
        }
        items(unidad.estado.siguientesPermitidos().sortedBy { it.ordinal }) { nuevo ->
            OutlinedButton(
                onClick = { acciones.onSolicitarCambio(nuevo) },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(etiquetaCambio(nuevo))
            }
        }

        // ---------- Historial ----------
        item { TituloSeccion("Historial") }
        if (estado.historial.isEmpty()) {
            item { Text("Sin cambios registrados.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(estado.historial, key = { "historial-${it.id}" }) { FilaHistorial(it) }
    }

    estado.estadoPorConfirmar?.let { nuevo ->
        AlertDialog(
            onDismissRequest = acciones.onCancelarCambio,
            title = { Text("¿${etiquetaCambio(nuevo)} la unidad?") },
            text = {
                Text(
                    if (nuevo == EstadoContenido.PUBLICADO) {
                        "Los estudiantes de los cursos que la tengan asignada la podrán ver."
                    } else {
                        "La unidad dejará de mostrarse a todos."
                    }
                )
            },
            confirmButton = { TextButton(onClick = acciones.onConfirmarCambio) { Text(etiquetaCambio(nuevo)) } },
            dismissButton = { TextButton(onClick = acciones.onCancelarCambio) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun FormularioOa(estado: UnidadDetalleUiState, acciones: AccionesUnidadDetalle) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Agregar OA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CampoTexto(
                valor = estado.codigoOa,
                onValorChange = acciones.onCodigoOaChange,
                etiqueta = "Código (ej: OA 3)",
                error = estado.errorCodigoOa,
                enabled = !estado.guardando
            )
            CampoTexto(
                valor = estado.descripcionOa,
                onValorChange = acciones.onDescripcionOaChange,
                etiqueta = "Descripción",
                error = estado.errorDescripcionOa,
                multilinea = true,
                enabled = !estado.guardando
            )
            BotonGrande(texto = "Agregar OA", onClick = acciones.onAgregarOa, cargando = estado.guardando)
        }
    }
}

@Composable
private fun FormularioActividad(estado: UnidadDetalleUiState, acciones: AccionesUnidadDetalle) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nueva actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = estado.tipoActividad == TipoActividad.DESAFIO,
                    onClick = { acciones.onTipoActividadChange(TipoActividad.DESAFIO) },
                    label = { Text(TipoActividad.DESAFIO.nombreVisible) }
                )
                FilterChip(
                    selected = estado.tipoActividad == TipoActividad.PRUEBA_FINAL,
                    onClick = { acciones.onTipoActividadChange(TipoActividad.PRUEBA_FINAL) },
                    enabled = !estado.tienePruebaFinal,
                    label = { Text(TipoActividad.PRUEBA_FINAL.nombreVisible) }
                )
            }
            if (estado.tienePruebaFinal) {
                Text(
                    "Esta unidad ya tiene prueba final",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CampoTexto(
                valor = estado.tituloActividad,
                onValorChange = acciones.onTituloActividadChange,
                etiqueta = "Título",
                error = estado.errorTituloActividad,
                enabled = !estado.guardando
            )
            CampoTexto(
                valor = estado.instruccionActividad,
                onValorChange = acciones.onInstruccionActividadChange,
                etiqueta = "Instrucción para el estudiante",
                error = estado.errorInstruccionActividad,
                enabled = !estado.guardando
            )
            BotonGrande(texto = "Crear actividad", onClick = acciones.onCrearActividad, cargando = estado.guardando)
        }
    }
}

@Composable
private fun FilaActividad(actividad: ActividadResumen, onClick: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(actividad.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${actividad.tipo.nombreVisible} · ${textoPreguntas(actividad.cantidadPreguntas)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun FilaHistorial(cambio: HistorialConUsuario) {
    val quien = cambio.usuarioNombre ?: "Alguien"
    val desde = cambio.estadoAnterior?.nombreVisible ?: "Nueva"
    val linea = buildString {
        append(fechaHora(cambio.fecha))
        append(" · ")
        append("$quien: $desde → ${cambio.estadoNuevo.nombreVisible}")
        cambio.comentario?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
    }
    Text(linea, style = MaterialTheme.typography.bodyMedium)
}

private fun textoPreguntas(cantidad: Int): String = if (cantidad == 1) "1 pregunta" else "$cantidad preguntas"

private val FORMATO_FECHA_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun fechaHora(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(FORMATO_FECHA_HORA)
