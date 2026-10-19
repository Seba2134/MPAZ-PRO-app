package cl.duoc.mpazpro.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

private const val DURACION_TRANSICION = 350

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
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier,
        // Al avanzar: la pantalla nueva entra desde la derecha y aparece suavemente
        enterTransition = {
            slideInHorizontally(tween(DURACION_TRANSICION)) { ancho -> ancho / 4 } +
                    fadeIn(tween(DURACION_TRANSICION))
        },
        exitTransition = {
            fadeOut(tween(DURACION_TRANSICION))
        },
        // Al volver: la pantalla actual sale hacia la derecha
        popEnterTransition = {
            fadeIn(tween(DURACION_TRANSICION))
        },
        popExitTransition = {
            slideOutHorizontally(tween(DURACION_TRANSICION)) { ancho -> ancho / 4 } +
                    fadeOut(tween(DURACION_TRANSICION))
        }
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