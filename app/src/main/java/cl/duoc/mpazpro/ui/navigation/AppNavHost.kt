package cl.duoc.mpazpro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.ui.docente.DocenteInicioScreen
import cl.duoc.mpazpro.ui.estudiante.EstudianteInicioScreen
import cl.duoc.mpazpro.ui.inicio.SeleccionRolScreen
import cl.duoc.mpazpro.ui.utp.UtpInicioScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO,
        modifier = modifier
    ) {
        composable(Rutas.INICIO) {
            SeleccionRolScreen(
                onRolSeleccionado = { rol ->
                    navController.navigate(rutaInicioDe(rol))
                }
            )
        }
        composable(Rutas.UTP_INICIO) {
            UtpInicioScreen(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.DOCENTE_INICIO) {
            DocenteInicioScreen(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.ESTUDIANTE_INICIO) {
            EstudianteInicioScreen(onVolver = { navController.popBackStack() })
        }
    }
}

/** Traduce un rol a la ruta de su pantalla inicial. */
private fun rutaInicioDe(rol: Rol): String = when (rol) {
    Rol.ESTUDIANTE -> Rutas.ESTUDIANTE_INICIO
    Rol.DOCENTE -> Rutas.DOCENTE_INICIO
    Rol.JEFATURA_UTP -> Rutas.UTP_INICIO
}