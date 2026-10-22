package cl.duoc.mpazpro.ui.docente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.relation.PruebaDocente
import cl.duoc.mpazpro.ui.components.BotonGrande
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Versión "conectada": obtiene el ViewModel con el id del docente. */
@Composable
fun DocenteInicioRoute(
    docenteId: Long,
    nombre: String,
    onConfigurarPrueba: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: DocenteViewModel = viewModel(factory = DocenteViewModel.factory(docenteId))
) {
    val pruebas by viewModel.pruebas.collectAsState()

    DocenteInicioScreen(
        nombre = nombre,
        pruebas = pruebas,
        onConfigurarPrueba = onConfigurarPrueba,
        onCerrarSesion = onCerrarSesion
    )
}

@Composable
fun DocenteInicioScreen(
    nombre: String,
    pruebas: List<PruebaDocente>,
    onConfigurarPrueba: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("¡Hola, $nombre!", style = MaterialTheme.typography.headlineMedium)
                Text("Pruebas de tus cursos", style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (pruebas.isEmpty()) {
            item {
                Text(
                    text = "Todavía no tienes unidades asignadas en tus cursos.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(pruebas, key = { it.asignacionId }) { prueba ->
            TarjetaPrueba(
                prueba = prueba,
                onConfigurar = { onConfigurarPrueba(prueba.asignacionId) }
            )
        }

        item {
            BotonGrande(
                texto = "Cerrar sesión",
                onClick = onCerrarSesion,
                colorFondo = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/** Una tarjeta por prueba, con una franja del color de la asignatura. */
@Composable
private fun TarjetaPrueba(prueba: PruebaDocente, onConfigurar: () -> Unit) {
    val colorAsignatura = Color(prueba.colorHex.toColorInt())

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(colorAsignatura)
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = prueba.asignaturaNombre,
                    color = colorAsignatura,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = prueba.unidadTitulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(text = prueba.cursoNombre, style = MaterialTheme.typography.bodyMedium)
                Text(text = resumenConfiguracion(prueba), style = MaterialTheme.typography.bodyMedium)
                BotonGrande(texto = "Configurar prueba", onClick = onConfigurar)
            }
        }
    }
}

private val FORMATO_CORTO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

/** "2 intentos · Sin fecha límite" o "3 intentos · Cierra el 25/10". */
private fun resumenConfiguracion(prueba: PruebaDocente): String {
    val intentos = if (prueba.intentosMaxPrueba == 1) "1 intento" else "${prueba.intentosMaxPrueba} intentos"
    val cierre = prueba.fechaFinPrueba?.let { millis ->
        val fecha = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        "Cierra el ${fecha.format(FORMATO_CORTO)}"
    } ?: "Sin fecha límite"
    return "$intentos · $cierre"
}