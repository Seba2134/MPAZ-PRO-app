package cl.duoc.mpazpro.ui.docente

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.ui.components.Encabezado
import cl.duoc.mpazpro.ui.components.TarjetaMensaje
import cl.duoc.mpazpro.ui.theme.VerdeHoja

@Composable
fun AsignarUnidadesRoute(
    onVolver: () -> Unit,
    viewModel: AsignarUnidadesViewModel = viewModel(factory = AsignarUnidadesViewModel.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    AsignarUnidadesScreen(
        estado = estado,
        onElegirCurso = viewModel::elegirCurso,
        onAsignar = viewModel::asignar,
        onMensajeVisto = viewModel::mensajeVisto,
        onVolver = onVolver
    )
}

@Composable
fun AsignarUnidadesScreen(
    estado: AsignarUnidadesUiState,
    onElegirCurso: (Long) -> Unit,
    onAsignar: (Long) -> Unit,
    onMensajeVisto: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Encabezado(titulo = "Asignar unidades", onVolver = onVolver) }

        item { TarjetaMensaje(mensaje = estado.mensaje, onVisto = onMensajeVisto) }

        if (estado.cargando) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            return@LazyColumn
        }

        if (estado.cursos.isEmpty()) {
            item { Text("No tienes cursos a cargo todavía.", style = MaterialTheme.typography.bodyLarge) }
            return@LazyColumn
        }

        // ---------- Elegir curso ----------
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Curso", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estado.cursos.forEach { curso ->
                        FilterChip(
                            selected = curso.id == estado.cursoId,
                            onClick = { onElegirCurso(curso.id) },
                            label = { Text(curso.nombre) }
                        )
                    }
                }
            }
        }

        when {
            estado.cursoId == null -> item {
                Text("Elige un curso para ver sus unidades.", style = MaterialTheme.typography.bodyLarge)
            }

            estado.asignaturas.isEmpty() -> item {
                Text("No tienes asignaturas en este curso.", style = MaterialTheme.typography.bodyLarge)
            }

            else -> items(estado.asignaturas, key = { it.asignatura.id }) { asignatura ->
                TarjetaAsignaturaUnidades(
                    asignatura = asignatura,
                    asignandoId = estado.asignandoId,
                    onAsignar = onAsignar
                )
            }
        }
    }
}

@Composable
private fun TarjetaAsignaturaUnidades(
    asignatura: AsignaturaConUnidades,
    asignandoId: Long?,
    onAsignar: (Long) -> Unit
) {
    val color = Color(asignatura.asignatura.colorHex.toColorInt())
    OutlinedCard(modifier = Modifier.fillMaxWidth(), border = BorderStroke(2.dp, color)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = asignatura.asignatura.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (asignatura.unidades.isEmpty()) {
                Text("UTP aún no autoriza unidades de esta asignatura.", style = MaterialTheme.typography.bodyMedium)
            }
            asignatura.unidades.forEach { item ->
                HorizontalDivider()
                FilaUnidadAsignable(
                    item = item,
                    asignando = asignandoId == item.unidad.id,
                    onAsignar = { onAsignar(item.unidad.id) }
                )
            }
        }
    }
}

@Composable
private fun FilaUnidadAsignable(item: UnidadAsignable, asignando: Boolean, onAsignar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.unidad.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(item.unidad.estado.nombreVisible, style = MaterialTheme.typography.bodyMedium)
            }
            if (item.asignada) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeHoja)
                Text(" Asignada", color = VerdeHoja, fontWeight = FontWeight.Bold)
            } else {
                OutlinedButton(onClick = onAsignar, enabled = !asignando) {
                    Text(if (asignando) "Asignando…" else "Asignar")
                }
            }
        }
        if (item.unidad.estado == EstadoContenido.AUTORIZADO) {
            Text(
                text = "Los estudiantes la verán cuando UTP la publique",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
