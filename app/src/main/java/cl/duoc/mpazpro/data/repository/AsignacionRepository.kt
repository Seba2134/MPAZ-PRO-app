package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.dao.AsignacionDao
import cl.duoc.mpazpro.data.local.dao.DocenteAsignaturaDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.entity.AsignacionEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.domain.model.EstadoContenido
import kotlinx.coroutines.flow.Flow

/** Posibles resultados al intentar asignar una unidad. */
enum class ResultadoAsignacion {
    ASIGNADA,       // todo bien
    NO_DISPONIBLE,  // la unidad no existe o UTP aún no la autoriza
    SIN_ACCESO,     // el docente no hace esa asignatura en ese curso
    YA_ASIGNADA     // el curso ya tenía esa unidad
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
     * Configura los intentos y las fechas de la prueba final.
     * @return false si los datos no son válidos o el docente no tiene acceso.
     */
    suspend fun configurarPrueba(
        docenteId: Long,
        asignacion: AsignacionEntity,
        intentosMax: Int,
        fechaInicio: Long?,
        fechaFin: Long?
    ): Boolean {
        if (intentosMax < 1) return false
        if (fechaInicio != null && fechaFin != null && fechaFin <= fechaInicio) return false

        val unidad = unidadDao.obtenerPorId(asignacion.unidadId) ?: return false
        if (!docenteAsignaturaDao.tieneAcceso(docenteId, asignacion.cursoId, unidad.asignaturaId)) return false

        asignacionDao.actualizar(
            asignacion.copy(
                intentosMaxPrueba = intentosMax,
                fechaInicioPrueba = fechaInicio,
                fechaFinPrueba = fechaFin
            )
        )
        return true
    }

    // ---------- Estudiante ----------

    fun asignaturasDelEstudiante(cursoId: Long): Flow<List<AsignaturaEntity>> =
        asignacionDao.obtenerAsignaturasDelEstudiante(cursoId)

    fun unidadesDelEstudiante(cursoId: Long, asignaturaId: Long): Flow<List<UnidadEntity>> =
        asignacionDao.obtenerUnidadesDelEstudiante(cursoId, asignaturaId)
}