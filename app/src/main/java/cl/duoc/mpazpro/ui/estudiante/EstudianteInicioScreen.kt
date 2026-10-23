package cl.duoc.mpazpro.ui.estudiante

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.relation.UnidadEstudiante
import cl.duoc.mpazpro.ui.components.BotonGrande
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Versión "conectada": obtiene el ViewModel con el curso del estudiante. */
@Composable
fun EstudianteInicioRoute(
    cursoId: Long?,
    nombre: String,
    onCerrarSesion: () -> Unit,
    viewModel: EstudianteViewModel = viewModel(factory = EstudianteViewModel.factory(cursoId))
) {
    val estado by viewModel.uiState.collectAsState()

    EstudianteInicioScreen(
        nombre = nombre,
        estado = estado,
        onCerrarSesion = onCerrarSesion
    )
}

@Composable
fun EstudianteInicioScreen(
    nombre: String,
    estado: EstudianteUiState,
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
                if (estado.cursoNombre.isNotEmpty()) {
                    Text("Tu curso: ${estado.cursoNombre}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        when {
            estado.cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            estado.sinCurso -> item {
                Text(
                    text = "Todavía no tienes un curso asignado. Pide ayuda a tu profesora o profesor.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            estado.asignaturas.isEmpty() -> item {
                Text(
                    text = "Por ahora no tienes unidades por cursar. ¡Vuelve pronto!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                item {
                    Text(
                        text = "Lo que tienes por cursar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(estado.asignaturas, key = { it.id }) { asignatura ->
                    TarjetaAsignatura(asignatura)
                }
            }
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

/** Una tarjeta por asignatura. Al tocar el encabezado se abre o se cierra con animación. */
@Composable
private fun TarjetaAsignatura(asignatura: AsignaturaPorCursar) {
    val colorAsignatura = Color(asignatura.colorHex.toColorInt())
    var abierta by rememberSaveable(asignatura.id) { mutableStateOf(true) }
    val giroFlecha by animateFloatAsState(if (abierta) 180f else 0f, label = "giroFlecha")

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(2.dp, colorAsignatura)
    ) {
        Column {
            // Encabezado: punto de color + nombre + cantidad + flecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { abierta = !abierta }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(colorAsignatura, CircleShape)
                )
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
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    asignatura.unidades.forEach { unidad ->
                        HorizontalDivider()
                        FilaUnidad(unidad)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaUnidad(unidad: UnidadEstudiante) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = unidad.unidadTitulo,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        if (unidad.instrucciones.isNotBlank()) {
            Text(
                text = "Mensaje del docente: “${unidad.instrucciones}”",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
        Text(
            text = estadoPrueba(unidad),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun textoCantidad(cantidad: Int): String =
    if (cantidad == 1) "1 unidad por cursar" else "$cantidad unidades por cursar"

private val FORMATO_CORTO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun fechaCorta(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATO_CORTO)

/** El estado de la prueba final según sus fechas, en palabras simples. */
private fun estadoPrueba(unidad: UnidadEstudiante, ahora: Long = System.currentTimeMillis()): String {
    val intentos = if (unidad.intentosMaxPrueba == 1) "1 intento" else "${unidad.intentosMaxPrueba} intentos"
    val inicio = unidad.fechaInicioPrueba
    val fin = unidad.fechaFinPrueba
    return when {
        inicio != null && ahora < inicio -> "Prueba final: abre el ${fechaCorta(inicio)}"
        fin != null && ahora > fin -> "Prueba final: cerrada"
        fin != null -> "Prueba final: disponible hasta el ${fechaCorta(fin)} · $intentos"
        else -> "Prueba final: disponible · $intentos"
    }
}