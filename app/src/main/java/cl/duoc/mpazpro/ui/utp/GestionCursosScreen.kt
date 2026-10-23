package cl.duoc.mpazpro.ui.utp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.components.MensajeError

/** Versión "conectada": obtiene el ViewModel y le pasa los eventos a la pantalla. */
@Composable
fun GestionCursosRoute(
    onVolver: () -> Unit,
    viewModel: GestionCursosViewModel = viewModel(factory = GestionCursosViewModel.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    GestionCursosScreen(
        estado = estado,
        onNombreChange = viewModel::onNombreChange,
        onProfesorJefeChange = viewModel::onProfesorJefeChange,
        onCrearCurso = viewModel::crearCurso,
        onCambiarDocente = viewModel::cambiarDocente,
        onEliminarCurso = viewModel::eliminarCurso,
        onMensajeVisto = viewModel::mensajeVisto,
        onVolver = onVolver
    )
}

@Composable
fun GestionCursosScreen(
    estado: GestionCursosUiState,
    onNombreChange: (String) -> Unit,
    onProfesorJefeChange: (Long) -> Unit,
    onCrearCurso: () -> Unit,
    onCambiarDocente: (cursoId: Long, asignaturaId: Long, docenteId: Long?) -> Unit,
    onEliminarCurso: (CursoResumen) -> Unit,
    onMensajeVisto: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El curso que se quiere eliminar: si no es null, se muestra el diálogo de confirmación
    var cursoPorEliminar by remember { mutableStateOf<CursoResumen?>(null) }

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
                Text("Cursos", style = MaterialTheme.typography.headlineMedium)
            }
        }

        // ---------- Mensaje (curso creado, error, etc.) ----------
        item {
            AnimatedVisibility(visible = estado.mensaje != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = estado.mensaje.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onMensajeVisto) { Text("OK") }
                    }
                }
            }
        }

        // ---------- Nuevo curso ----------
        item {
            FormularioNuevoCurso(
                estado = estado,
                onNombreChange = onNombreChange,
                onProfesorJefeChange = onProfesorJefeChange,
                onCrearCurso = onCrearCurso
            )
        }

        // ---------- Cursos existentes ----------
        item {
            Text(
                text = "Cursos del colegio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            estado.cargando -> item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            estado.cursos.isEmpty() -> item {
                Text("Todavía no hay cursos.", style = MaterialTheme.typography.bodyLarge)
            }

            else -> items(estado.cursos, key = { it.id }) { curso ->
                TarjetaCurso(
                    curso = curso,
                    asignaturas = estado.asignaturas,
                    docentes = estado.docentes,
                    onCambiarDocente = { asignaturaId, docenteId ->
                        onCambiarDocente(curso.id, asignaturaId, docenteId)
                    },
                    onEliminar = { cursoPorEliminar = curso }
                )
            }
        }
    }

    // ---------- Confirmación antes de eliminar ----------
    cursoPorEliminar?.let { curso ->
        AlertDialog(
            onDismissRequest = { cursoPorEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar ${curso.nombre}?") },
            text = { Text("También se quitarán las unidades asignadas y los docentes de este curso. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarCurso(curso)
                    cursoPorEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { cursoPorEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioNuevoCurso(
    estado: GestionCursosUiState,
    onNombreChange: (String) -> Unit,
    onProfesorJefeChange: (Long) -> Unit,
    onCrearCurso: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nuevo curso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            CampoTexto(
                valor = estado.nombreNuevo,
                onValorChange = onNombreChange,
                etiqueta = "Nombre del curso (ej: 2.º B)",
                icono = Icons.Filled.Home,
                error = estado.errorNombre,
                enabled = !estado.creando
            )

            Text("Profesor jefe", style = MaterialTheme.typography.bodyLarge)
            if (estado.docentes.isEmpty()) {
                Text("Primero crea una cuenta de docente.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    estado.docentes.forEach { docente ->
                        FilterChip(
                            selected = docente.id == estado.profesorJefeId,
                            onClick = { onProfesorJefeChange(docente.id) },
                            label = { Text(docente.nombre) },
                            enabled = !estado.creando
                        )
                    }
                }
            }
            MensajeError(estado.errorProfesorJefe)

            BotonGrande(texto = "Crear curso", onClick = onCrearCurso, cargando = estado.creando)
        }
    }
}

/** Un curso: al tocar el encabezado se abre su carga académica. */
@Composable
private fun TarjetaCurso(
    curso: CursoResumen,
    asignaturas: List<AsignaturaEntity>,
    docentes: List<UsuarioEntity>,
    onCambiarDocente: (asignaturaId: Long, docenteId: Long?) -> Unit,
    onEliminar: () -> Unit
) {
    var abierta by rememberSaveable(curso.id) { mutableStateOf(false) }
    val giroFlecha by animateFloatAsState(if (abierta) 180f else 0f, label = "giroFlecha")

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { abierta = !abierta }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(curso.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Profesor jefe: ${curso.profesorJefeNombre}", style = MaterialTheme.typography.bodyMedium)
                    Text(textoEstudiantes(curso.cantidadEstudiantes), style = MaterialTheme.typography.bodyMedium)
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (abierta) "Ocultar detalle" else "Ver detalle",
                    modifier = Modifier.rotate(giroFlecha)
                )
            }

            // Detalle: docentes por asignatura + eliminar
            AnimatedVisibility(visible = abierta) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()
                    Text("Docentes por asignatura", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "El profesor jefe ya ve todas las asignaturas. Aquí se elige al profesor de cada una.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    asignaturas.forEach { asignatura ->
                        FilaDocente(
                            asignatura = asignatura,
                            docenteActualId = curso.docentePorAsignatura[asignatura.id],
                            docentes = docentes,
                            onElegir = { docenteId -> onCambiarDocente(asignatura.id, docenteId) }
                        )
                    }

                    HorizontalDivider()
                    val puedeEliminarse = curso.cantidadEstudiantes == 0
                    OutlinedButton(onClick = onEliminar, enabled = puedeEliminarse) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Text("Eliminar curso", modifier = Modifier.padding(start = 8.dp))
                    }
                    if (!puedeEliminarse) {
                        Text(
                            text = "Solo se puede eliminar un curso sin estudiantes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** "Matemática: Carlos ▾". Al tocar el nombre se abre un menú con los docentes. */
@Composable
private fun FilaDocente(
    asignatura: AsignaturaEntity,
    docenteActualId: Long?,
    docentes: List<UsuarioEntity>,
    onElegir: (Long?) -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val nombreActual = docentes.firstOrNull { it.id == docenteActualId }?.nombre ?: "Sin asignar"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(asignatura.nombre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Box {
            TextButton(onClick = { menuAbierto = true }) {
                Text(nombreActual)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Cambiar docente de ${asignatura.nombre}")
            }
            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                DropdownMenuItem(
                    text = { Text("Sin asignar") },
                    onClick = {
                        onElegir(null)
                        menuAbierto = false
                    }
                )
                docentes.forEach { docente ->
                    DropdownMenuItem(
                        text = { Text(docente.nombre) },
                        onClick = {
                            onElegir(docente.id)
                            menuAbierto = false
                        }
                    )
                }
            }
        }
    }
}

private fun textoEstudiantes(cantidad: Int): String = when (cantidad) {
    0 -> "Sin estudiantes"
    1 -> "1 estudiante"
    else -> "$cantidad estudiantes"
}