package cl.duoc.mpazpro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.ui.docente.DocenteInicioScreen
import cl.duoc.mpazpro.ui.estudiante.EstudianteInicioScreen
import cl.duoc.mpazpro.ui.login.LoginRoute
import cl.duoc.mpazpro.ui.utp.UtpInicioScreen

@Composable
fun AppNavHost(
    sesion: Sesion,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val usuario by sesion.usuarioActual.collectAsState()

    // Cada vez que cambia la sesión, se decide a dónde ir.
    LaunchedEffect(usuario) {
        val destino = usuario?.let { rutaInicioDe(it.rol) } ?: Rutas.LOGIN
        if (navController.currentDestination?.route != destino) {
            navController.navigate(destino) {
                // Borra todo el historial: no se puede "volver" al login estando
                // conectado, ni volver a una pantalla privada después de cerrar sesión.
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier
    ) {
        composable(Rutas.LOGIN) {
            LoginRoute()
        }
        composable(Rutas.UTP_INICIO) {
            UtpInicioScreen(
                nombre = usuario?.nombre.orEmpty(),
                onCerrarSesion = sesion::cerrar
            )
        }
        composable(Rutas.DOCENTE_INICIO) {
            DocenteInicioScreen(
                nombre = usuario?.nombre.orEmpty(),
                onCerrarSesion = sesion::cerrar
            )
        }
        composable(Rutas.ESTUDIANTE_INICIO) {
            EstudianteInicioScreen(
                nombre = usuario?.nombre.orEmpty(),
                onCerrarSesion = sesion::cerrar
            )
        }
    }
}

/** Traduce un rol a la ruta de su pantalla inicial. */
private fun rutaInicioDe(rol: Rol): String = when (rol) {
    Rol.ESTUDIANTE -> Rutas.ESTUDIANTE_INICIO
    Rol.DOCENTE -> Rutas.DOCENTE_INICIO
    Rol.JEFATURA_UTP -> Rutas.UTP_INICIO
}