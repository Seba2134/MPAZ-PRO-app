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
import cl.duoc.mpazpro.ui.inicio.SeleccionRolScreen
import cl.duoc.mpazpro.ui.navigation.AppNavHost
import cl.duoc.mpazpro.ui.theme.ColorLenguaje
import cl.duoc.mpazpro.ui.theme.MPAZPROTheme
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // TEMPORAL: comprobar que Room funciona. Borrar después.
        // TEMPORAL: comprobar que Room funciona. Borrar después.
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            Log.d("MPAZ4", "Asignaturas: ${db.asignaturaDao().contar()}")
            Log.d("MPAZ4", "Usuarios: ${db.usuarioDao().contar()}")

            val acceso = db.docenteAsignaturaDao()
            // Ana (2) es profesora jefe: debería tener acceso a Lenguaje (1)
            Log.d("MPAZ4", "Ana → Lenguaje: ${acceso.tieneAcceso(docenteId = 2, cursoId = 1, asignaturaId = 1)}")
            // Carlos (3) hace Matemática (2): sí
            Log.d("MPAZ4", "Carlos → Matemática: ${acceso.tieneAcceso(docenteId = 3, cursoId = 1, asignaturaId = 2)}")
            // Carlos (3) NO hace Lenguaje (1): no
            Log.d("MPAZ4", "Carlos → Lenguaje: ${acceso.tieneAcceso(docenteId = 3, cursoId = 1, asignaturaId = 1)}")
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