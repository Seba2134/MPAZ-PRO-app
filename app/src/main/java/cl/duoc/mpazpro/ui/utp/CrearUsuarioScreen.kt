package cl.duoc.mpazpro.ui.utp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioScreen(
    uiState: CrearUsuarioUiState,
    onRunChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAlternarMostrarPassword: () -> Unit,
    onRolChange: (Rol) -> Unit,
    onCursoChange: (Long) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear usuario") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- 1) Tipo de cuenta (decide si se pide curso) ----------
            Text("Tipo de cuenta", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

            // ---------- 2) Curso: solo aparece para estudiantes ----------
            AnimatedVisibility(
                visible = uiState.rolSeleccionado == Rol.ESTUDIANTE,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Curso del estudiante", style = MaterialTheme.typography.bodyLarge)

                    if (uiState.cursos.isEmpty()) {
                        Text(
                            text = "No hay cursos creados todavía.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.cursos.forEach { curso ->
                                FilterChip(
                                    selected = curso.id == uiState.cursoSeleccionadoId,
                                    onClick = { onCursoChange(curso.id) },
                                    label = { Text(curso.nombre) }
                                )
                            }
                        }
                    }

                    MensajeError(uiState.errorCurso)
                }
            }

            // ---------- 3) Datos de la persona ----------
            CampoTexto(
                valor = uiState.nombre,
                onValorChange = onNombreChange,
                etiqueta = "Nombre completo",
                icono = Icons.Filled.Person,
                error = uiState.errorNombre,
                enabled = !uiState.cargando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            CampoTexto(
                valor = uiState.run,
                onValorChange = onRunChange,
                etiqueta = "RUN (opcional)",
                enabled = !uiState.cargando,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            CampoTexto(
                valor = uiState.correo,
                onValorChange = onCorreoChange,
                etiqueta = "Correo (opcional)",
                icono = Icons.Filled.Email,
                error = uiState.errorCorreo,
                enabled = !uiState.cargando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            // ---------- 4) Credenciales ----------
            CampoTexto(
                valor = uiState.nombreUsuario,
                onValorChange = onUsuarioChange,
                etiqueta = "Usuario para iniciar sesión",
                icono = Icons.Filled.AccountCircle,
                error = uiState.errorUsuario,
                enabled = !uiState.cargando,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    autoCorrectEnabled = false
                )
            )

            CampoTexto(
                valor = uiState.password,
                onValorChange = onPasswordChange,
                etiqueta = "Contraseña",
                icono = Icons.Filled.Lock,
                error = uiState.errorPassword,
                esPassword = true,
                mostrarPassword = uiState.mostrarPassword,
                onAlternarMostrarPassword = onAlternarMostrarPassword,
                enabled = !uiState.cargando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            // ---------- 5) Error general y botón ----------
            MensajeError(uiState.errorGeneral)

            BotonGrande(
                texto = if (uiState.cargando) "Guardando..." else "Guardar usuario",
                onClick = onGuardar,
                cargando = uiState.cargando
            )
        }
    }
}

/** Mensaje de error con ícono, que aparece y desaparece con animación. */
@Composable
private fun MensajeError(mensaje: String?) {
    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = mensaje.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}