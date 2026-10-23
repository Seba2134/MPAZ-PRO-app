package cl.duoc.mpazpro.ui.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.ui.components.BotonGrande

@Composable
fun UtpInicioScreen(
    nombre: String,
    onCrearUsuario: () -> Unit,
    onCrearContenido: () -> Unit, // <-- Nuevo parámetro
    onCerrarSesion: () -> Unit,
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
        Text("Hola, $nombre", style = MaterialTheme.typography.headlineMedium)
        Text("Panel de Jefatura UTP", style = MaterialTheme.typography.bodyLarge)

        // Botón para ir al formulario de creación de contenidos (Cámara/Micrófono)
        BotonGrande(
            texto = "Crear contenido",
            onClick = onCrearContenido
        )

        // Botón para ir al formulario de creación de usuarios
        BotonGrande(
            texto = "Crear nuevo usuario",
            onClick = onCrearUsuario
        )

        BotonGrande(
            texto = "Cerrar sesión",
            onClick = onCerrarSesion,
            colorFondo = MaterialTheme.colorScheme.secondary
        )
    }
}