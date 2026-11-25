package cl.duoc.mpazpro.ui.docente

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import cl.duoc.mpazpro.ui.components.Encabezado
import cl.duoc.mpazpro.ui.theme.VerdeHoja

@Composable
fun AvanceRoute(
    asignacionId: Long,
    onVolver: () -> Unit,
    onVerInforme: (Long) -> Unit,
    viewModel: AvanceViewModel = viewModel(factory = AvanceViewModel.factory(asignacionId))
) {
    val estado by viewModel.uiState.collectAsState()
    AvanceScreen(estado = estado, onVolver = onVolver, onVerInforme = onVerInforme)
}

@Composable
fun AvanceScreen(
    estado: AvanceUiState,
    onVolver: () -> Unit,
    onVerInforme: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Encabezado(titulo = "Avance del curso", onVolver = onVolver) }

        if (estado.unidad.isNotEmpty()) {
            item {
                Column {
                    Text(estado.unidad, style = MaterialTheme.typography.titleLarge)
                    Text("${estado.asignatura} · ${estado.curso}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        when {
            estado.error != null -> item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("  ${estado.error}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            estado.cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            estado.estudiantes.isEmpty() -> item {
                Text("Este curso no tiene estudiantes activos.", style = MaterialTheme.typography.bodyLarge)
            }

            else -> {
                item {
                    Text(
                        text = "${estado.rindieron} de ${estado.estudiantes.size} estudiantes rindieron",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(estado.estudiantes, key = { it.estudianteId }) { fila ->
                    FilaAvance(
                        fila = fila,
                        intentosMax = estado.intentosMax,
                        onClick = { onVerInforme(fila.estudianteId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaAvance(fila: AvanceEstudiante, intentosMax: Int, onClick: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.Person, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(fila.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${fila.estado.nombreVisible} · ${fila.intentosUsados} de $intentosMax intentos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            fila.mejorPuntaje?.let {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeHoja)
                Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
