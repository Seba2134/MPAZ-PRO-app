package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Campo de fecha: se ve como un campo de texto, pero al tocarlo llama a [onClick]
 * (para abrir el calendario). No se puede escribir a mano, así no hay fechas mal escritas.
 */
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box {
            OutlinedTextField(
                value = fecha?.format(FORMATO_FECHA).orEmpty(),
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                isError = error != null,
                label = { Text(etiqueta) },
                placeholder = { Text("Toca para elegir") },
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                trailingIcon = if (error != null) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Campo con error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Una capa invisible encima del campo que recibe el toque
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = enabled, onClick = onClick)
            )
        }
        MensajeError(error, modifier = Modifier.padding(start = 12.dp, top = 6.dp))
    }
}