package cl.duoc.mpazpro.ui.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hola, $nombre", style = MaterialTheme.typography.headlineMedium)
        Text("Panel de Jefatura UTP", style = MaterialTheme.typography.bodyLarge)
        BotonGrande(texto = "Cerrar sesión", onClick = onCerrarSesion)
    }
}