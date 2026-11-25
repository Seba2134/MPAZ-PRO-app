package cl.duoc.mpazpro.ui.utp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
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
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.components.Encabezado
import cl.duoc.mpazpro.ui.components.TarjetaMensaje

@Composable
fun UnidadesRoute(
    onVolver: () -> Unit,
    onAbrirUnidad: (Long) -> Unit,
    viewModel: UnidadesViewModel = viewModel(factory = UnidadesViewModel.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    UnidadesScreen(
        estado = estado,
        onElegirAsignatura = viewModel::elegirAsignatura,
        onTituloChange = viewModel::onTituloChange,
        onCrearUnidad = viewModel::crearUnidad,
        onMensajeVisto = viewModel::mensajeVisto,
        onAbrirUnidad = onAbrirUnidad,
        onVolver = onVolver
    )
}

@Composable
fun UnidadesScreen(
    estado: UnidadesUiState,
    onElegirAsignatura: (Long) -> Unit,
    onTituloChange: (String) -> Unit,
    onCrearUnidad: () -> Unit,
    onMensajeVisto: () -> Unit,
    onAbrirUnidad: (Long) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Encabezado(titulo = "Unidades", onVolver = onVolver) }

        // ---------- Elegir asignatura ----------
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                estado.asignaturas.forEach { asignatura ->
                    FilterChip(
                        selected = asignatura.id == estado.asignaturaId,
                        onClick = { onElegirAsignatura(asignatura.id) },
                        label = { Text(asignatura.nombre) }
                    )
                }
            }
        }

        item { TarjetaMensaje(mensaje = estado.mensaje, onVisto = onMensajeVisto) }

        // ---------- Nueva unidad ----------
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nueva unidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    CampoTexto(
                        valor = estado.titulo,
                        onValorChange = onTituloChange,
                        etiqueta = "Título de la unidad",
                        error = estado.errorTitulo,
                        enabled = !estado.guardando
                    )
                    BotonGrande(
                        texto = "Crear unidad",
                        onClick = onCrearUnidad,
                        enabled = estado.asignaturaId != null,
                        cargando = estado.guardando
                    )
                }
            }
        }

        // ---------- Unidades de la asignatura ----------
        if (estado.unidades.isEmpty()) {
            item { Text("Esta asignatura todavía no tiene unidades.", style = MaterialTheme.typography.bodyLarge) }
        }
        items(estado.unidades, key = { it.id }) { unidad ->
            TarjetaUnidad(unidad = unidad, onClick = { onAbrirUnidad(unidad.id) })
        }
    }
}

@Composable
private fun TarjetaUnidad(unidad: UnidadEntity, onClick: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = unidad.titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            ChipEstado(unidad.estado)
        }
    }
}
