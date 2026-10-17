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

            // Usuarios y permisos
            val usuarios = container.usuarioRepository
            val estudiantes = usuarios.perfilesPorRol(Rol.ESTUDIANTE).first()
            Log.d("MPAZ2", "Estudiantes: ${estudiantes.map { it.nombre }}")
            Log.d("MPAZ2", "Carlos → Lenguaje: ${usuarios.tieneAcceso(docenteId = 3, cursoId = 1, asignaturaId = 1)}")

            // Reglas del flujo de estados (sin tocar la base de datos)
            Log.d("MPAZ2", "Borrador → Publicado: ${EstadoContenido.BORRADOR.puedeCambiarA(EstadoContenido.PUBLICADO)}")
            Log.d("MPAZ2", "Borrador → En revisión: ${EstadoContenido.BORRADOR.puedeCambiarA(EstadoContenido.EN_REVISION)}")
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