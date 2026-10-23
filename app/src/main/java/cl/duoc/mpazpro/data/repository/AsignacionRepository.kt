package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.dao.AsignacionDao
import cl.duoc.mpazpro.data.local.dao.DocenteAsignaturaDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.entity.AsignacionEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.data.local.relation.PruebaDocente
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.Flow

/** Posibles resultados al intentar asignar una unidad. */
enum class ResultadoAsignacion {
    ASIGNADA,       // todo bien
    NO_DISPONIBLE,  // la unidad no existe o UTP aún no la autoriza
    SIN_ACCESO,     // el docente no hace esa asignatura en ese curso
    YA_ASIGNADA     // el curso ya tenía esa unidad
}

/** Posibles resultados al guardar la configuración de una prueba. */
enum class ResultadoConfiguracion {
    GUARDADA,
    DATOS_INVALIDOS,
    SIN_ACCESO,
    NO_ENCONTRADA
}

/** El docente asigna unidades a su curso y configura la prueba; el estudiante ve lo asignado. */
class AsignacionRepository(
    private val asignacionDao: AsignacionDao,
    private val unidadDao: UnidadDao,
    private val docenteAsignaturaDao: DocenteAsignaturaDao
) {

    // ---------- Docente ----------

    fun unidadesDisponibles(asignaturaId: Long): Flow<List<UnidadEntity>> =
        asignacionDao.obtenerUnidadesDisponibles(asignaturaId)

    /** Las pruebas que el docente puede configurar (ya filtradas por sus permisos). */
    fun pruebasDelDocente(docenteId: Long): Flow<List<PruebaDocente>> =
        asignacionDao.obtenerPruebasDelDocente(docenteId)

    suspend fun detallePrueba(asignacionId: Long): PruebaDocente? =
        asignacionDao.obtenerDetallePrueba(asignacionId)

    suspend fun asignacionPorId(asignacionId: Long): AsignacionEntity? =
        asignacionDao.obtenerPorId(asignacionId)

    fun asignacionesDelCurso(cursoId: Long): Flow<List<AsignacionEntity>> =
        asignacionDao.obtenerPorCurso(cursoId)

    suspend fun asignacion(cursoId: Long, unidadId: Long): AsignacionEntity? =
        asignacionDao.obtener(cursoId, unidadId)

    /** Asigna una unidad al curso, verificando estado, permisos y duplicados. */
    suspend fun asignarUnidad(docenteId: Long, cursoId: Long, unidadId: Long): ResultadoAsignacion {
        val unidad = unidadDao.obtenerPorId(unidadId) ?: return ResultadoAsignacion.NO_DISPONIBLE

        val estadoValido = unidad.estado == EstadoContenido.AUTORIZADO ||
                unidad.estado == EstadoContenido.PUBLICADO
        if (!estadoValido) return ResultadoAsignacion.NO_DISPONIBLE

        if (!docenteAsignaturaDao.tieneAcceso(docenteId, cursoId, unidad.asignaturaId)) {
            return ResultadoAsignacion.SIN_ACCESO
        }

        if (asignacionDao.obtener(cursoId, unidadId) != null) return ResultadoAsignacion.YA_ASIGNADA

        asignacionDao.asignar(
            AsignacionEntity(cursoId = cursoId, unidadId = unidadId, asignadoPorId = docenteId)
        )
        return ResultadoAsignacion.ASIGNADA
    }

    /**
     * Guarda la configuración de la prueba final, verificando los datos y los permisos del docente.
     * Las fechas van en milisegundos; null significa "sin restricción".
     */
    suspend fun configurarPrueba(
        docenteId: Long,
        asignacionId: Long,
        instrucciones: String,
        intentosMax: Int,
        fechaInicio: Long?,
        fechaFin: Long?,
        mostrarRespuestas: Boolean
    ): ResultadoConfiguracion {
        // 1) Datos válidos (la última protección, aunque la pantalla ya validó)
        if (instrucciones.isBlank()) return ResultadoConfiguracion.DATOS_INVALIDOS
        if (intentosMax !in Validaciones.INTENTOS_MINIMOS..Validaciones.INTENTOS_MAXIMOS) {
            return ResultadoConfiguracion.DATOS_INVALIDOS
        }
        if (fechaInicio != null && fechaFin != null && fechaFin <= fechaInicio) {
            return ResultadoConfiguracion.DATOS_INVALIDOS
        }

        // 2) La prueba existe
        val asignacion = asignacionDao.obtenerPorId(asignacionId) ?: return ResultadoConfiguracion.NO_ENCONTRADA
        val unidad = unidadDao.obtenerPorId(asignacion.unidadId) ?: return ResultadoConfiguracion.NO_ENCONTRADA

        // 3) El docente tiene acceso a esa asignatura en ese curso
        if (!docenteAsignaturaDao.tieneAcceso(docenteId, asignacion.cursoId, unidad.asignaturaId)) {
            return ResultadoConfiguracion.SIN_ACCESO
        }

        // 4) Guardar
        asignacionDao.actualizar(
            asignacion.copy(
                instrucciones = instrucciones,
                intentosMaxPrueba = intentosMax,
                fechaInicioPrueba = fechaInicio,
                fechaFinPrueba = fechaFin,
                mostrarRespuestas = mostrarRespuestas
            )
        )
        return ResultadoConfiguracion.GUARDADA
    }

    // ---------- Estudiante ----------

    fun asignaturasDelEstudiante(cursoId: Long): Flow<List<AsignaturaEntity>> =
        asignacionDao.obtenerAsignaturasDelEstudiante(cursoId)

    fun unidadesDelEstudiante(cursoId: Long, asignaturaId: Long): Flow<List<UnidadEntity>> =
        asignacionDao.obtenerUnidadesDelEstudiante(cursoId, asignaturaId)
}