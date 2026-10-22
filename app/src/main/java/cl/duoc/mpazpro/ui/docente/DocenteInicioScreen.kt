package cl.duoc.mpazpro.ui.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.ui.components.BotonGrande

@Composable
fun DocenteInicioScreen(
    nombre: String,
    onTomarAsistencia: () -> Unit,
    onRegistrarNotas: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera de bienvenida
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("¡Hola, $nombre!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Panel Docente · Escuela Marcela Paz", style = MaterialTheme.typography.bodyLarge)
        }

        // Tarjeta con los accesos principales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gestión Aula 2.º Básico",
                    style = MaterialTheme.typography.titleMedium
                )

                BotonGrande(
                    texto = "Registrar Asistencia",
                    onClick = onTomarAsistencia
                )

                BotonGrande(
                    texto = "Ingresar Calificaciones",
                    onClick = onRegistrarNotas
                )
            }
        }

        // Botón inferior de cerrar sesión
        BotonGrande(
            texto = "Cerrar sesión",
            onClick = onCerrarSesion
        )
    }
}