package cl.duoc.mpazpro.ui.utp

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.relation.PreguntaConOpciones
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.components.Encabezado
import cl.duoc.mpazpro.ui.components.MensajeError
import cl.duoc.mpazpro.ui.components.TarjetaMensaje
import cl.duoc.mpazpro.ui.theme.VerdeHoja

@Composable
fun ActividadUtpRoute(
    actividadId: Long,
    onVolver: () -> Unit,
    viewModel: ActividadUtpViewModel = viewModel(factory = ActividadUtpViewModel.factory(actividadId))
) {
    val estado by viewModel.uiState.collectAsState()

    ActividadUtpScreen(
        estado = estado,
        onOaChange = viewModel::onOaChange,
        onEnunciadoChange = viewModel::onEnunciadoChange,
        onAlternativaChange = viewModel::onAlternativaChange,
        onCorrectaChange = viewModel::onCorrectaChange,
        onGuardar = viewModel::guardarPregunta,
        onMensajeVisto = viewModel::mensajeVisto,
        onVolver = onVolver
    )
}

@Composable
fun ActividadUtpScreen(
    estado: ActividadUtpUiState,
    onOaChange: (Long) -> Unit,
    onEnunciadoChange: (String) -> Unit,
    onAlternativaChange: (Int, String) -> Unit,
    onCorrectaChange: (Int) -> Unit,
    onGuardar: () -> Unit,
    onMensajeVisto: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actividad = estado.actividad

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Encabezado(titulo = actividad?.titulo ?: "Actividad", onVolver = onVolver) }

        if (estado.cargando) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            return@LazyColumn
        }
        if (actividad == null) {
            item { Text("No se encontró esta actividad.", style = MaterialTheme.typography.bodyLarge) }
            return@LazyColumn
        }

        item {
            Column {
                Text(actividad.tipo.nombreVisible, style = MaterialTheme.typography.titleMedium)
                Text(actividad.instruccion, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item { TarjetaMensaje(mensaje = estado.mensaje, onVisto = onMensajeVisto) }

        // ---------- Preguntas ----------
        item { Text("Preguntas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (estado.preguntas.isEmpty()) {
            item { Text("Todavía no tiene preguntas.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(estado.preguntas, key = { it.pregunta.id }) { pregunta ->
            TarjetaPregunta(pregunta = pregunta, codigoOa = estado.codigoOa(pregunta.pregunta.oaId))
        }

        // ---------- Nueva pregunta ----------
        if (!estado.enBorrador) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("  Para editar, vuelve la unidad a Borrador", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            item { FormularioPregunta(estado, onOaChange, onEnunciadoChange, onAlternativaChange, onCorrectaChange, onGuardar) }
        }
    }
}

@Composable
private fun TarjetaPregunta(pregunta: PreguntaConOpciones, codigoOa: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${pregunta.pregunta.orden}. ${pregunta.pregunta.enunciado}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(codigoOa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            pregunta.opciones.forEach { opcion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (opcion.esCorrecta) {
                        Icon(Icons.Filled.Check, contentDescription = "Correcta", tint = VerdeHoja)
                        Text(" ${opcion.texto} (correcta)", color = VerdeHoja, fontWeight = FontWeight.Bold)
                    } else {
                        Text("•  ${opcion.texto}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioPregunta(
    estado: ActividadUtpUiState,
    onOaChange: (Long) -> Unit,
    onEnunciadoChange: (String) -> Unit,
    onAlternativaChange: (Int, String) -> Unit,
    onCorrectaChange: (Int) -> Unit,
    onGuardar: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nueva pregunta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // OA de la unidad: opción única con RadioButton en toda la fila
            Text("Objetivo de aprendizaje", style = MaterialTheme.typography.bodyLarge)
            if (estado.objetivos.isEmpty()) {
                Text("Primero agrega un OA a la unidad.", style = MaterialTheme.typography.bodyMedium)
            }
            estado.objetivos.forEach { oa ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = estado.oaId == oa.id,
                            onClick = { onOaChange(oa.id) },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.oaId == oa.id, onClick = null)
                    Text("${oa.codigo} · ${oa.descripcion}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            MensajeError(estado.errorOa)

            CampoTexto(
                valor = estado.enunciado,
                onValorChange = onEnunciadoChange,
                etiqueta = "Enunciado",
                error = estado.errorEnunciado,
                multilinea = true,
                enabled = !estado.guardando
            )

            Text("Alternativas (marca la correcta)", style = MaterialTheme.typography.bodyLarge)
            estado.alternativas.forEachIndexed { indice, texto ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = estado.correcta == indice,
                        onClick = { onCorrectaChange(indice) }
                    )
                    CampoTexto(
                        valor = texto,
                        onValorChange = { onAlternativaChange(indice, it) },
                        etiqueta = "Alternativa ${'A' + indice}",
                        enabled = !estado.guardando,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            MensajeError(estado.errorAlternativas)

            BotonGrande(texto = "Guardar pregunta", onClick = onGuardar, cargando = estado.guardando)
        }
    }
}
