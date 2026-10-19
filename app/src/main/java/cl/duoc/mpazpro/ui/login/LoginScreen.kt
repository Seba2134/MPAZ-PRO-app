package cl.duoc.mpazpro.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.theme.MPAZPROTheme
import kotlin.math.roundToInt

/** Versión "conectada": obtiene el ViewModel y le pasa su estado a la pantalla. */
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

/** Versión "pura": solo dibuja lo que recibe y avisa los eventos. */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onNombreUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAlternarMostrarPassword: () -> Unit,
    onEntrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación de "sacudida" cada vez que falla un intento
    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(uiState.intentosFallidos) {
        if (uiState.intentosFallidos > 0) {
            for (posicion in listOf(18f, -18f, 12f, -12f, 6f, -6f, 0f)) {
                sacudida.animateTo(posicion, animationSpec = tween(durationMillis = 45))
            }
        }
    }

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

        // El formulario completo se desplaza a los lados con la sacudida
        Column(
            modifier = Modifier.offset { IntOffset(sacudida.value.roundToInt(), 0) },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoTexto(
                valor = uiState.nombreUsuario,
                onValorChange = onNombreUsuarioChange,
                etiqueta = "Usuario",
                icono = Icons.Filled.Person,
                error = uiState.errorUsuario,
                enabled = !uiState.cargando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
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
                ),
                keyboardActions = KeyboardActions(onDone = { onEntrar() })
            )
        }

        // Error general con ícono y animación
        AnimatedVisibility(
            visible = uiState.errorGeneral != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = uiState.errorGeneral.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        BotonGrande(
            texto = if (uiState.cargando) "Entrando..." else "Entrar",
            onClick = onEntrar,
            cargando = uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

