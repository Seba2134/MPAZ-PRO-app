package cl.duoc.mpazpro.ui.estudiante

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.domain.model.TipoContenido
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.ImagenArchivo
import cl.duoc.mpazpro.ui.theme.AmarilloClaro
import cl.duoc.mpazpro.ui.theme.AmarilloSol
import cl.duoc.mpazpro.ui.theme.AzulClaro
import cl.duoc.mpazpro.ui.theme.AzulPrincipal
import cl.duoc.mpazpro.ui.theme.RojoError
import cl.duoc.mpazpro.ui.theme.TextoOscuro
import cl.duoc.mpazpro.ui.theme.VerdeClaro
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
            BannerEstudiante(
                titulo = estado.titulo.ifEmpty { "Unidad" },
                emoji = "📘",
                onVolver = onVolver
            )
        }

        when {
            estado.cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            estado.sinAcceso -> item {
                MensajeConEmoji(emoji = "🔒", texto = "Esta unidad no está disponible")
            }

            else -> {
                // ---------- Avance ----------
                item { TarjetaAvance(estado.avance) }

                // ---------- Aprende ----------
                item { TituloSeccion("📚", "Aprende") }
                if (estado.objetivos.isEmpty()) {
                    item { MensajeConEmoji("🌱", "Esta unidad todavía no tiene contenidos.") }
                }
                items(estado.objetivos, key = { "oa-${it.objetivo.id}" }) { oa ->
                    TarjetaObjetivo(
                        oa = oa,
                        numero = estado.objetivos.indexOf(oa) + 1,
                        reproduciendoId = estado.reproduciendoId,
                        onAlternarAudio = onAlternarAudio
                    )
                }

                // ---------- Desafíos ----------
                item { TituloSeccion("🎯", "Desafíos") }
                if (estado.desafios.isEmpty()) {
                    item { Text("Esta unidad no tiene desafíos.", style = MaterialTheme.typography.bodyLarge) }
                }
                items(estado.desafios, key = { "desafio-${it.id}" }) { desafio ->
                    TarjetaDesafio(
                        desafio = desafio,
                        hecho = desafio.id in estado.terminadas,
                        onClick = { onAbrirActividad(desafio.id) }
                    )
                }

                // ---------- Prueba final ----------
                item { TituloSeccion("🏆", "Prueba final") }
                item { TarjetaPruebaFinal(estado = estado, onRendir = onAbrirActividad) }
            }
        }
    }
}

/** Cuánto lleva de la unidad, con un anillo y un mensaje de ánimo. */
@Composable
private fun TarjetaAvance(avance: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AzulClaro.copy(alpha = 0.5f), contentColor = TextoOscuro)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnilloAvance(avance)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tu avance", style = MaterialTheme.typography.bodyMedium)
                Text(mensajeAvance(avance), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Un OA con todos sus contenidos, cada uno dibujado según su tipo. */
@Composable
private fun TarjetaObjetivo(
    oa: OaConContenidos,
    numero: Int,
    reproduciendoId: Long?,
    onAlternarAudio: (ContenidoEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Insignia con el número + código y descripción del OA
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AzulPrincipal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$numero", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = oa.objetivo.codigo,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AzulPrincipal
                    )
                    Text(text = oa.objetivo.descripcion, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            if (oa.contenidos.isEmpty()) {
                Text("Todavía no hay contenido para este objetivo.", style = MaterialTheme.typography.bodyMedium)
            }
            oa.contenidos.forEach { contenido ->
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AmarilloClaro, contentColor = TextoOscuro),
            border = BorderStroke(2.dp, AmarilloSol.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("💡 Ejemplo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Button(
                    onClick = onAlternarAudio,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (reproduciendo) RojoError else VerdeHoja
                    ),
                    modifier = Modifier.heightIn(min = 56.dp)
                ) {
                    Icon(
                        imageVector = if (reproduciendo) Icons.Filled.Close else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Text(
                        text = if (reproduciendo) "  Detener" else "  🔊 Escuchar",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Text("Audio no disponible", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Una tarjeta grande por desafío. Si ya lo terminó, lo dice con ícono y texto (puede repetirlo igual). */
@Composable
private fun TarjetaDesafio(desafio: ActividadEntity, hecho: Boolean, onClick: () -> Unit) {
    val color = if (hecho) VerdeHoja else AmarilloSol
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f), contentColor = TextoOscuro),
        border = BorderStroke(2.dp, color.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(desafio.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (hecho) {
                    Pildora("✓ Hecho", fondo = VerdeHoja, colorTexto = Color.White)
                    Text("Puedes repetirlo", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Toca para jugar", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Icon(Icons.Filled.PlayArrow, contentDescription = "Abrir desafío", tint = color, modifier = Modifier.size(36.dp))
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AzulClaro.copy(alpha = 0.45f), contentColor = TextoOscuro),
        border = BorderStroke(3.dp, AzulPrincipal)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(prueba.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IndicadorIntentos(usados = estado.intentosPrueba.size, maximo = asignacion.intentosMaxPrueba)

            val fechas = listOfNotNull(
                asignacion.fechaInicioPrueba?.let { "📅 Abre el ${fechaCorta(it)}" },
                asignacion.fechaFinPrueba?.let { "⏰ Cierra el ${fechaCorta(it)}" }
            )
            if (fechas.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fechas.forEach { Pildora(it, fondo = Color.White, colorTexto = TextoOscuro) }
                }
            }
            if (prueba.id in estado.terminadas) {
                Pildora("✓ Ya la rendiste", fondo = VerdeClaro, colorTexto = VerdeHoja)
            }
            BotonGrande(
                texto = estadoPrueba?.textoBoton ?: "Rendir prueba",
                onClick = { onRendir(prueba.id) },
                enabled = estadoPrueba?.puedeRendir == true
            )
            estadoPrueba?.motivo?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = RojoError)
                    Text("  $it", style = MaterialTheme.typography.bodyLarge, color = RojoError, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private val FORMATO_CORTO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun fechaCorta(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATO_CORTO)
