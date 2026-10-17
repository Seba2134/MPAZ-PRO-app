package cl.duoc.mpazpro.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.theme.MPAZPROTheme

/**
 * Versión "conectada": obtiene el ViewModel y le pasa su estado a la pantalla.
 * Es la que usa la navegación.
 */
@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        onNombreUsuarioChange = viewModel::onNombreUsuarioChange,
        onPasswordChange = viewModel::onPasswordChange,
        onAlternarMostrarPassword = viewModel::alternarMostrarPassword,
        onEntrar = viewModel::iniciarSesion,
        modifier = modifier
    )
}

/**
 * Versión "pura": solo dibuja lo que recibe y avisa los eventos.
 * No conoce el ViewModel, por eso se puede ver en el @Preview.
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onNombreUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAlternarMostrarPassword: () -> Unit,
    onEntrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MPAZ PRO 2.º",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Inicia sesión",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = uiState.nombreUsuario,
            onValueChange = onNombreUsuarioChange,
            label = { Text("Usuario") },
            singleLine = true,
            enabled = !uiState.cargando,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !uiState.cargando,
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = if (uiState.mostrarPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onEntrar() }),
            trailingIcon = {
                TextButton(onClick = onAlternarMostrarPassword) {
                    Text(
                        text = if (uiState.mostrarPassword) "Ocultar" else "Ver",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        uiState.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        BotonGrande(
            texto = if (uiState.cargando) "Entrando..." else "Entrar",
            onClick = onEntrar,
            enabled = !uiState.cargando
        )
    }
}
