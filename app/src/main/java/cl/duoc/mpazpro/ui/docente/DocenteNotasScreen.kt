package cl.duoc.mpazpro.ui.docente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.ui.components.BotonGrande

data class EstudianteNotaItem(
    val id: Long,
    val nombre: String,
    var nota: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteNotasScreen(
    onVolver: () -> Unit
) {
    var estudiantes by remember {
        mutableStateOf(
            listOf(
                EstudianteNotaItem(1, "Juan Pérez", "6.5"),
                EstudianteNotaItem(2, "María González", "7.0"),
                EstudianteNotaItem(3, "Lucas Silva", "5.8")
            )
        )
    }
    var guardadoExitoso by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingreso de Notas - 2.º Básico") },
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
                text = "Asignatura: Matemáticas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
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
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = estudiante.nota,
                            onValueChange = { nuevaNota ->
                                estudiantes = estudiantes.map {
                                    if (it.id == estudiante.id) it.copy(nota = nuevaNota) else it
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    HorizontalDivider()
                }
            }

            if (guardadoExitoso) {
                Text(
                    text = "¡Notas registradas con éxito!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
            }

            BotonGrande(
                texto = "Guardar Calificaciones",
                onClick = { guardadoExitoso = true }
            )
        }
    }
}