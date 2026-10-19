package cl.duoc.mpazpro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de texto estándar de la app: etiqueta, ícono, error con ícono y animación.
 * Si [error] no es null, el campo se pinta de rojo y aparece el mensaje debajo.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    icono: ImageVector? = null,
    esPassword: Boolean = false,
    mostrarPassword: Boolean = false,
    onAlternarMostrarPassword: () -> Unit = {},
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val hayError = error != null

    // Ícono a la izquierda (opcional)
    val iconoInicial: (@Composable () -> Unit)? = if (icono != null) {
        { Icon(imageVector = icono, contentDescription = null) }
    } else {
        null
    }

    // A la derecha: el botón "Ver/Ocultar" en contraseñas, o un ícono de alerta si hay error
    val iconoFinal: (@Composable () -> Unit)? = when {
        esPassword -> {
            {
                TextButton(onClick = onAlternarMostrarPassword) {
                    Text(
                        text = if (mostrarPassword) "Ocultar" else "Ver",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        hayError -> {
            {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        else -> null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            label = { Text(etiqueta) },
            singleLine = true,
            enabled = enabled,
            isError = hayError,
            textStyle = MaterialTheme.typography.bodyLarge,
            leadingIcon = iconoInicial,
            trailingIcon = iconoFinal,
            visualTransformation = if (esPassword && !mostrarPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier.fillMaxWidth()
        )

        // El mensaje de error aparece y desaparece con animación
        AnimatedVisibility(
            visible = hayError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, top = 6.dp),
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
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}