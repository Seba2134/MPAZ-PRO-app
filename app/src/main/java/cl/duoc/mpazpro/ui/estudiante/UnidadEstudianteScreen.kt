package cl.duoc.mpazpro.ui.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.domain.model.TipoContenido
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.ImagenArchivo
import cl.duoc.mpazpro.ui.theme.AmarilloClaro
import cl.duoc.mpazpro.ui.theme.TextoOscuro
import cl.duoc.mpazpro.ui.theme.VerdeHoja
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Versión "conectada": obtiene el ViewModel de esta unidad. */
@Composable
fun UnidadEstudianteRoute(
    unidadId: Long,
    onVolver: () -> Unit,
    onAbrirActividad: (Long) -> Unit,
    viewModel: UnidadEstudianteViewModel = viewModel(factory = UnidadEstudianteViewModel.factory(unidadId))
) {
    val estado by viewModel.uiState.collectAsState()

    UnidadEstudianteScreen(
        estado = estado,
        onVolver = onVolver,
        onAbrirActividad = onAbrirActividad,
        onAlternarAudio = viewModel::alternarAudio
    )
}

@Composable
fun UnidadEstudianteScreen(
    estado: UnidadEstudianteUiState,
    onVolver: () -> Unit,
    onAbrirActividad: (Long) -> Unit,
    onAlternarAudio: (ContenidoEntity) -> Unit,
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
                    text = estado.titulo.ifEmpty { "Unidad" },
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        when {
            estado.cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            estado.sinAcceso -> item {
                Text("Esta unidad no está disponible", style = MaterialTheme.typography.bodyLarge)
            }

            else -> {
                // ---------- Avance ----------
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Avance: ${estado.avance} %", style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(
                            progress = { estado.avance / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ---------- Aprende ----------
                item { TituloSeccion("Aprende") }
                if (estado.objetivos.isEmpty()) {
                    item { Text("Esta unidad todavía no tiene contenidos.", style = MaterialTheme.typography.bodyLarge) }
                }
                items(estado.objetivos, key = { "oa-${it.objetivo.id}" }) { oa ->
                    TarjetaObjetivo(
                        oa = oa,
                        reproduciendoId = estado.reproduciendoId,
                        onAlternarAudio = onAlternarAudio
                    )
                }

                // ---------- Desafíos ----------
                item { TituloSeccion("Desafíos") }
                if (estado.desafios.isEmpty()) {
                    item { Text("Esta unidad no tiene desafíos.", style = MaterialTheme.typography.bodyLarge) }
                }
                items(estado.desafios, key = { "desafio-${it.id}" }) { desafio ->
                    BotonDesafio(
                        desafio = desafio,
                        hecho = desafio.id in estado.terminadas,
                        onClick = { onAbrirActividad(desafio.id) }
                    )
                }

                // ---------- Prueba final ----------
                item { TituloSeccion("Prueba final") }
                item {
                    TarjetaPruebaFinal(estado = estado, onRendir = onAbrirActividad)
                }
            }
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(text = texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

/** Un OA con todos sus contenidos, cada uno dibujado según su tipo. */
@Composable
private fun TarjetaObjetivo(
    oa: OaConContenidos,
    reproduciendoId: Long?,
    onAlternarAudio: (ContenidoEntity) -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = oa.objetivo.codigo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = oa.objetivo.descripcion, style = MaterialTheme.typography.bodyLarge)

            if (oa.contenidos.isEmpty()) {
                Text("Todavía no hay contenido para este objetivo.", style = MaterialTheme.typography.bodyMedium)
            }
            oa.contenidos.forEach { contenido ->
                HorizontalDivider()
                VistaContenido(
                    contenido = contenido,
                    reproduciendo = reproduciendoId == contenido.id,
                    onAlternarAudio = { onAlternarAudio(contenido) }
                )
            }
        }
    }
}

@Composable
private fun VistaContenido(
    contenido: ContenidoEntity,
    reproduciendo: Boolean,
    onAlternarAudio: () -> Unit
) {
    when (contenido.tipo) {
        TipoContenido.TEXTO -> Text(contenido.texto.orEmpty(), style = MaterialTheme.typography.bodyLarge)

        TipoContenido.EJEMPLO -> Card(
            colors = CardDefaults.cardColors(containerColor = AmarilloClaro, contentColor = TextoOscuro),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ejemplo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(contenido.texto.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            }
        }

        TipoContenido.IMAGEN -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val ruta = contenido.recurso
            if (ruta != null) {
                ImagenArchivo(ruta = ruta, descripcion = contenido.texto ?: "Imagen del contenido")
            } else {
                Text("Imagen no disponible", style = MaterialTheme.typography.bodyMedium)
            }
            contenido.texto?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }

        TipoContenido.AUDIO -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            contenido.texto?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
            val existe = remember(contenido.recurso) { contenido.recurso?.let { File(it).exists() } == true }
            if (existe) {
                OutlinedButton(
                    onClick = onAlternarAudio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    Icon(
                        imageVector = if (reproduciendo) Icons.Filled.Close else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Text(
                        text = if (reproduciendo) "  Detener" else "  Escuchar",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Text("Audio no disponible", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Un botón grande por desafío. Si ya lo terminó, lo dice con ícono y texto (puede repetirlo igual). */
@Composable
private fun BotonDesafio(desafio: ActividadEntity, hecho: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
    ) {
        Text(
            text = desafio.titulo,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (hecho) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeHoja)
            Text(" Hecho", color = VerdeHoja, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TarjetaPruebaFinal(estado: UnidadEstudianteUiState, onRendir: (Long) -> Unit) {
    val prueba = estado.pruebaFinal
    val asignacion = estado.asignacion
    if (prueba == null || asignacion == null) {
        Text("Esta unidad todavía no tiene prueba final.", style = MaterialTheme.typography.bodyLarge)
        return
    }
    val estadoPrueba = estado.estadoPrueba

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(prueba.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Intentos: ${estado.intentosPrueba.size} de ${asignacion.intentosMaxPrueba}",
                style = MaterialTheme.typography.bodyLarge
            )
            asignacion.fechaInicioPrueba?.let {
                Text("Abre el ${fechaCorta(it)}", style = MaterialTheme.typography.bodyMedium)
            }
            asignacion.fechaFinPrueba?.let {
                Text("Cierra el ${fechaCorta(it)}", style = MaterialTheme.typography.bodyMedium)
            }
            if (prueba.id in estado.terminadas) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeHoja)
                    Text(" Ya la rendiste", color = VerdeHoja, fontWeight = FontWeight.Bold)
                }
            }
            BotonGrande(
                texto = estadoPrueba?.textoBoton ?: "Rendir prueba",
                onClick = { onRendir(prueba.id) },
                enabled = estadoPrueba?.puedeRendir == true
            )
            estadoPrueba?.motivo?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private val FORMATO_CORTO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun fechaCorta(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATO_CORTO)
