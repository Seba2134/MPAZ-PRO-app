package cl.duoc.mpazpro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import cl.duoc.mpazpro.ui.navigation.AppNavHost
import cl.duoc.mpazpro.ui.theme.MPAZPROTheme

class MainActivity : ComponentActivity() {

    // Si se rechaza, no pasa nada: la app funciona igual y los resultados quedan pendientes
    private val pedirRedLocal = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sesion = (application as MpazApp).container.sesion

        if (savedInstanceState == null) pedirPermisoRedLocal()

        setContent {
            MPAZPROTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        sesion = sesion,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Desde Android 17 (API 37) una app necesita permiso para conectarse a la red local.
     * El servidor de desarrollo (10.0.2.2 o la IP del PC) está en la red local, así que se pide
     * al abrir la app, antes de iniciar sesión, porque el token del servidor se pide al entrar.
     */
    private fun pedirPermisoRedLocal() {
        if (Build.VERSION.SDK_INT < API_RED_LOCAL) return
        val permiso = Manifest.permission.ACCESS_LOCAL_NETWORK
        if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
            pedirRedLocal.launch(permiso)
        }
    }

    private companion object {
        const val API_RED_LOCAL = 37
    }
}
