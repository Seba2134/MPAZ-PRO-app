package cl.duoc.mpazpro.ui.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.ui.components.BotonGrande

data class EstudianteAsistenciaItem(
    val id: Long,
    val nombre: String,
    var presente: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteAsistenciaScreen(
    onVolver: () -> Unit
) {
    // Lista de ejemplo para el 2.° Básico
    var estudiantes by remember {
        mutableStateOf(
            listOf(
                EstudianteAsistenciaItem(1, "Juan Pérez"),
                EstudianteAsistenciaItem(2, "María González"),
                EstudianteAsistenciaItem(3, "Lucas Silva")
            )
        )
    }
    var guardadoExitoso by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistencia - 2.º Básico") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Marcar estudiantes presentes:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(estudiantes) { estudiante ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = estudiante.nombre,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = estudiante.presente,
                            onCheckedChange = { esPresente ->
                                estudiantes = estudiantes.map {
                                    if (it.id == estudiante.id) it.copy(presente = esPresente) else it
                                }
                            }
                        )
                    }
                    HorizontalDivider()
                }
            }

            if (guardadoExitoso) {
                Text(
                    text = "¡Asistencia guardada correctamente!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
            }

            BotonGrande(
                texto = "Guardar Asistencia",
                onClick = { guardadoExitoso = true }
            )
        }
    }
}