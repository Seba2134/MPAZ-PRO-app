package cl.duoc.mpazpro

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import cl.duoc.mpazpro.data.local.AppDatabase
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.ui.inicio.SeleccionRolScreen
import cl.duoc.mpazpro.ui.navigation.AppNavHost
import cl.duoc.mpazpro.ui.theme.ColorLenguaje
import cl.duoc.mpazpro.ui.theme.MPAZPROTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // TEMPORAL: comprobar que Room funciona. Borrar después.
        // TEMPORAL: comprobar que Room funciona. Borrar después.
        lifecycleScope.launch {
            val container = (application as MpazApp).container
            val usuarios = container.usuarioRepository

            Log.d("MPAZ2", "ana / ana123 → ${usuarios.iniciarSesion("ana", "ana123")}")
            Log.d("MPAZ2", "ana / mala → ${usuarios.iniciarSesion("ana", "mala")}")
            Log.d("MPAZ2", "noexiste / 1234 → ${usuarios.iniciarSesion("noexiste", "1234")}")
            Log.d("MPAZ2", "  TOMAS  / 1234 → ${usuarios.iniciarSesion("  TOMAS  ", "1234")}")

            // UTP (id 1) crea un estudiante nuevo; Ana (id 2) intenta hacer lo mismo
            Log.d("MPAZ2", "UTP crea a Martina → ${usuarios.crearUsuario(1, "Martina", "martina", "1234", Rol.ESTUDIANTE, 1)}")
            Log.d("MPAZ2", "UTP repite 'martina' → ${usuarios.crearUsuario(1, "Martina B", "martina", "1234", Rol.ESTUDIANTE, 1)}")
            Log.d("MPAZ2", "Ana intenta crear → ${usuarios.crearUsuario(2, "Pedro", "pedro", "1234", Rol.ESTUDIANTE, 1)}")
        }
        setContent {
            MPAZPROTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}