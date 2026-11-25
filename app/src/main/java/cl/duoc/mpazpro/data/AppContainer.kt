package cl.duoc.mpazpro.data

import android.content.Context
import cl.duoc.mpazpro.data.local.AppDatabase
import cl.duoc.mpazpro.data.remote.ClienteApi
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.data.repository.ResultadoRepository
import cl.duoc.mpazpro.data.repository.SincronizacionRepository
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Crea la base de datos y los repositorios UNA sola vez para toda la app.
 * Las pantallas (a través de sus ViewModels) piden aquí lo que necesitan.
 */
class AppContainer(context: Context) {

    private val db = AppDatabase.getInstance(context)
    val sesion = Sesion()

    val contenidoRepository: ContenidoRepository by lazy {
        ContenidoRepository(
            asignaturaDao = db.asignaturaDao(),
            unidadDao = db.unidadDao(),
            objetivoDao = db.objetivoAprendizajeDao(),
            contenidoDao = db.contenidoDao(),
            actividadDao = db.actividadDao(),
            preguntaDao = db.preguntaDao(),
            opcionDao = db.opcionDao(),
            historialDao = db.historialCambioDao(),
            usuarioDao = db.usuarioDao()
        )
    }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(
            usuarioDao = db.usuarioDao(),
            cursoDao = db.cursoDao(),
            docenteAsignaturaDao = db.docenteAsignaturaDao()
        )
    }

    val asignacionRepository: AsignacionRepository by lazy {
        AsignacionRepository(
            asignacionDao = db.asignacionDao(),
            unidadDao = db.unidadDao(),
            docenteAsignaturaDao = db.docenteAsignaturaDao()
        )
    }

    val resultadoRepository: ResultadoRepository by lazy {
        ResultadoRepository(
            intentoDao = db.intentoDao(),
            respuestaDao = db.respuestaDao(),
            opcionDao = db.opcionDao(),
            preguntaDao = db.preguntaDao(),
            asignacionDao = db.asignacionDao(),
            observacionDao = db.observacionDocenteDao()
        )
    }

    /**
     * Para trabajos en segundo plano que deben terminar aunque se cierre la pantalla
     * (ej: pedir el token o enviar resultados justo después de salir de una actividad).
     */
    val alcanceApp: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val sincronizacionRepository: SincronizacionRepository by lazy {
        SincronizacionRepository(
            api = ClienteApi.crear(sesion),
            sesion = sesion,
            intentoDao = db.intentoDao(),
            respuestaDao = db.respuestaDao()
        )
    }
}