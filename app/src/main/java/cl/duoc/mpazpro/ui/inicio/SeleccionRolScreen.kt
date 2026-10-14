package cl.duoc.mpazpro.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.theme.MPAZPROTheme

@Composable
fun SeleccionRolScreen(
    onRolSeleccionado: (Rol) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
            text = "¿Quién eres?",
            style = MaterialTheme.typography.titleLarge
        )

        Rol.entries.forEach { rol ->
            BotonGrande(
                texto = rol.nombreVisible,
                onClick = { onRolSeleccionado(rol) }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun SeleccionRolPreview() {
    MPAZPROTheme {
        SeleccionRolScreen(onRolSeleccionado = { })
    }
}