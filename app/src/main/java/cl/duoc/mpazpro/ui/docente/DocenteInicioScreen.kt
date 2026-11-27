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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cl.duoc.mpazpro.data.local.relation.PruebaDocente
import cl.duoc.mpazpro.data.repository.textoEstadoServicio
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.TarjetaMensaje
import cl.duoc.mpazpro.ui.theme.VerdeHoja
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Versión "conectada": obtiene el ViewModel con el id del docente. */
@Composable
fun DocenteInicioRoute(
    docenteId: Long,
    nombre: String,
    onConfigurarPrueba: (Long) -> Unit,
    onAsignarUnidades: () -> Unit,
    onVerAvance: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: DocenteViewModel = viewModel(factory = DocenteViewModel.factory(docenteId))
) {
    val pruebas by viewModel.pruebas.collectAsState()
    val servidor by viewModel.servidor.collectAsState()
    val pendientes by viewModel.pendientes.collectAsState()

    DocenteInicioScreen(
        nombre = nombre,
        pruebas = pruebas,
        servidor = servidor,
        pendientes = pendientes,
        onSincronizar = viewModel::sincronizarAhora,
        onMensajeVisto = viewModel::mensajeVisto,
        onConfigurarPrueba = onConfigurarPrueba,
        onAsignarUnidades = onAsignarUnidades,
        onVerAvance = onVerAvance,
        onCerrarSesion = onCerrarSesion
    )
}

@Composable
fun DocenteInicioScreen(
    nombre: String,
    pruebas: List<PruebaDocente>,
    servidor: EstadoServidor,
    pendientes: Int,
    onSincronizar: () -> Unit,
    onMensajeVisto: () -> Unit,
    onConfigurarPrueba: (Long) -> Unit,
    onAsignarUnidades: () -> Unit,
    onVerAvance: (Long) -> Unit,
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

        item {
            TarjetaServidor(servidor = servidor, pendientes = pendientes, onSincronizar = onSincronizar)
        }
        item { TarjetaMensaje(mensaje = servidor.mensaje, onVisto = onMensajeVisto) }

        item {
            BotonGrande(texto = "Asignar unidades", onClick = onAsignarUnidades)
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
                onConfigurar = { onConfigurarPrueba(prueba.asignacionId) },
                onVerAvance = { onVerAvance(prueba.asignacionId) }
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

/** Estado de cada microservicio y de los resultados que faltan por enviar. */
@Composable
private fun TarjetaServidor(servidor: EstadoServidor, pendientes: Int, onSincronizar: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LineaServicio(nombre = "Usuarios", conectado = servidor.usuarios)
            LineaServicio(nombre = "Resultados", conectado = servidor.resultados)
            Text("Resultados pendientes de enviar: $pendientes", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSincronizar, enabled = !servidor.sincronizando) {
                Text(if (servidor.sincronizando) "Sincronizando…" else "Sincronizar ahora")
            }
        }
    }
}

/** Una línea por microservicio: ícono + «Usuarios: conectado ✓» (el color nunca va solo). */
@Composable
private fun LineaServicio(nombre: String, conectado: Boolean?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (conectado) {
            true -> Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeHoja)
            false -> Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            null -> Unit
        }
        Text(" " + textoEstadoServicio(nombre, conectado), style = MaterialTheme.typography.bodyLarge)
    }
}

/** Una tarjeta por prueba, con una franja del color de la asignatura. */
@Composable
private fun TarjetaPrueba(prueba: PruebaDocente, onConfigurar: () -> Unit, onVerAvance: () -> Unit) {
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
                TextButton(onClick = onVerAvance) { Text("Ver avance") }
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