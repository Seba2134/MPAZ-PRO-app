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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.ui.docente.AsignarUnidadesRoute
import cl.duoc.mpazpro.ui.docente.AvanceRoute
import cl.duoc.mpazpro.ui.docente.ConfigurarPruebaRoute
import cl.duoc.mpazpro.ui.docente.DocenteInicioRoute
import cl.duoc.mpazpro.ui.docente.InformeRoute
import cl.duoc.mpazpro.ui.estudiante.EstudianteInicioRoute
import cl.duoc.mpazpro.ui.estudiante.RendirActividadRoute
import cl.duoc.mpazpro.ui.estudiante.UnidadEstudianteRoute
import cl.duoc.mpazpro.ui.login.LoginRoute
import cl.duoc.mpazpro.ui.utp.ActividadUtpRoute
import cl.duoc.mpazpro.ui.utp.CrearUsuarioRoute
import cl.duoc.mpazpro.ui.utp.UnidadDetalleRoute
import cl.duoc.mpazpro.ui.utp.UnidadesRoute
import cl.duoc.mpazpro.ui.utp.CrearContenidoRoute
import cl.duoc.mpazpro.ui.utp.UtpInicioScreen
import cl.duoc.mpazpro.ui.utp.GestionCursosRoute
private const val DURACION_TRANSICION = 350

@Composable
fun AppNavHost(
    sesion: Sesion,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val usuario by sesion.usuarioActual.collectAsState()

    // El id del usuario para el que ya se navegó. Sobrevive al girar la pantalla,
    // así la navegación solo ocurre cuando alguien inicia o cierra sesión.
    var usuarioNavegadoId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(usuario?.id) {
        val idActual = usuario?.id
        if (idActual == usuarioNavegadoId) return@LaunchedEffect   // mismo usuario: no tocar la navegación
        usuarioNavegadoId = idActual

        val destino = usuario?.let { rutaInicioDe(it.rol) } ?: Rutas.LOGIN
        navController.navigate(destino) {
            // Borra el historial: no se puede volver al login estando conectado,
            // ni a una pantalla privada después de cerrar sesión.
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(tween(DURACION_TRANSICION)) { ancho -> ancho / 4 } +
                    fadeIn(tween(DURACION_TRANSICION))
        },
        exitTransition = {
            fadeOut(tween(DURACION_TRANSICION))
        },
        popEnterTransition = {
            fadeIn(tween(DURACION_TRANSICION))
        },
        popExitTransition = {
            slideOutHorizontally(tween(DURACION_TRANSICION)) { ancho -> ancho / 4 } +
                    fadeOut(tween(DURACION_TRANSICION))
        }
    ) {
        // ---------- Login ----------
        composable(Rutas.LOGIN) {
            LoginRoute()
        }

        // ---------- Jefatura UTP ----------
        // ---------- Jefatura UTP ----------
        composable(Rutas.UTP_INICIO) {
            UtpInicioScreen(
                nombre = usuario?.nombre.orEmpty(),
                onCrearUsuario = { navController.navigate(Rutas.UTP_CREAR_USUARIO) },
                onCrearContenido = { navController.navigate(Rutas.UTP_CREAR_CONTENIDO) },
                onGestionarCursos = { navController.navigate(Rutas.UTP_CURSOS) },
                onUnidades = { navController.navigate(Rutas.UTP_UNIDADES) },
                onCerrarSesion = sesion::cerrar
            )
        }
        composable(Rutas.UTP_CREAR_USUARIO) {
            CrearUsuarioRoute(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.UTP_CREAR_CONTENIDO) {
            CrearContenidoRoute(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.UTP_CURSOS) {
            GestionCursosRoute(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.UTP_UNIDADES) {
            UnidadesRoute(
                onVolver = { navController.popBackStack() },
                onAbrirUnidad = { unidadId -> navController.navigate(Rutas.unidadUtp(unidadId)) }
            )
        }
        composable(
            route = Rutas.UTP_UNIDAD,
            arguments = listOf(navArgument("unidadId") { type = NavType.LongType })
        ) { entrada ->
            UnidadDetalleRoute(
                unidadId = entrada.arguments?.getLong("unidadId") ?: 0L,
                onVolver = { navController.popBackStack() },
                onAbrirActividad = { actividadId -> navController.navigate(Rutas.actividadUtp(actividadId)) }
            )
        }
        composable(
            route = Rutas.UTP_ACTIVIDAD,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { entrada ->
            ActividadUtpRoute(
                actividadId = entrada.arguments?.getLong("actividadId") ?: 0L,
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------- Docente ----------
        composable(Rutas.DOCENTE_INICIO) {
            DocenteInicioRoute(
                docenteId = usuario?.id ?: 0L,
                nombre = usuario?.nombre.orEmpty(),
                onConfigurarPrueba = { asignacionId ->
                    navController.navigate(Rutas.configurarPrueba(asignacionId))
                },
                onAsignarUnidades = { navController.navigate(Rutas.DOCENTE_ASIGNAR) },
                onVerAvance = { asignacionId -> navController.navigate(Rutas.avance(asignacionId)) },
                onCerrarSesion = sesion::cerrar
            )
        }
        composable(
            route = Rutas.DOCENTE_CONFIGURAR_PRUEBA,
            arguments = listOf(navArgument("asignacionId") { type = NavType.LongType })
        ) { entrada ->
            ConfigurarPruebaRoute(
                asignacionId = entrada.arguments?.getLong("asignacionId") ?: 0L,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.DOCENTE_ASIGNAR) {
            AsignarUnidadesRoute(onVolver = { navController.popBackStack() })
        }
        composable(
            route = Rutas.DOCENTE_AVANCE,
            arguments = listOf(navArgument("asignacionId") { type = NavType.LongType })
        ) { entrada ->
            val asignacionId = entrada.arguments?.getLong("asignacionId") ?: 0L
            AvanceRoute(
                asignacionId = asignacionId,
                onVolver = { navController.popBackStack() },
                onVerInforme = { estudianteId -> navController.navigate(Rutas.informe(asignacionId, estudianteId)) }
            )
        }
        composable(
            route = Rutas.DOCENTE_INFORME,
            arguments = listOf(
                navArgument("asignacionId") { type = NavType.LongType },
                navArgument("estudianteId") { type = NavType.LongType }
            )
        ) { entrada ->
            InformeRoute(
                asignacionId = entrada.arguments?.getLong("asignacionId") ?: 0L,
                estudianteId = entrada.arguments?.getLong("estudianteId") ?: 0L,
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------- Estudiante ----------
        composable(Rutas.ESTUDIANTE_INICIO) {
            EstudianteInicioRoute(
                cursoId = usuario?.cursoId,
                nombre = usuario?.nombre.orEmpty(),
                onAbrirUnidad = { unidadId -> navController.navigate(Rutas.unidad(unidadId)) },
                onCerrarSesion = sesion::cerrar
            )
        }
        composable(
            route = Rutas.ESTUDIANTE_UNIDAD,
            arguments = listOf(navArgument("unidadId") { type = NavType.LongType })
        ) { entrada ->
            UnidadEstudianteRoute(
                unidadId = entrada.arguments?.getLong("unidadId") ?: 0L,
                onVolver = { navController.popBackStack() },
                onAbrirActividad = { actividadId -> navController.navigate(Rutas.actividad(actividadId)) }
            )
        }
        composable(
            route = Rutas.ESTUDIANTE_ACTIVIDAD,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { entrada ->
            RendirActividadRoute(
                actividadId = entrada.arguments?.getLong("actividadId") ?: 0L,
                onVolver = { navController.popBackStack() }
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