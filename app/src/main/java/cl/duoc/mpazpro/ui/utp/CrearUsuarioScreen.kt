package cl.duoc.mpazpro.ui.utp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.ui.components.BotonGrande

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioScreen(
    uiState: CrearUsuarioUiState,
    onRunChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRolChange: (Rol) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Usuario") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.run,
                onValueChange = onRunChange,
                label = { Text("RUT / RUN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.correo,
                onValueChange = onCorreoChange,
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.nombreUsuario,
                onValueChange = onUsuarioChange,
                label = { Text("Nombre de Usuario (Login)") },
                isError = uiState.errorUsuario != null,
                supportingText = uiState.errorUsuario?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.errorPassword != null,
                supportingText = uiState.errorPassword?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Seleccionar Rol:", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = uiState.rolSeleccionado == Rol.ESTUDIANTE,
                    onClick = { onRolChange(Rol.ESTUDIANTE) },
                    label = { Text("Estudiante") }
                )
                FilterChip(
                    selected = uiState.rolSeleccionado == Rol.DOCENTE,
                    onClick = { onRolChange(Rol.DOCENTE) },
                    label = { Text("Docente") }
                )
            }

            uiState.errorGeneral?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                BotonGrande(
                    texto = "Guardar Usuario",
                    onClick = onGuardar
                )
            }
        }
    }
}