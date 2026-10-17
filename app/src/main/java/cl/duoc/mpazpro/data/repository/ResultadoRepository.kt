package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.dao.AsignacionDao
import cl.duoc.mpazpro.data.local.dao.IntentoDao
import cl.duoc.mpazpro.data.local.dao.ObservacionDocenteDao
import cl.duoc.mpazpro.data.local.dao.OpcionDao
import cl.duoc.mpazpro.data.local.dao.PreguntaDao
import cl.duoc.mpazpro.data.local.dao.RespuestaDao
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.IntentoEntity
import cl.duoc.mpazpro.data.local.entity.ObservacionDocenteEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.data.local.relation.RendimientoOa
import cl.duoc.mpazpro.domain.model.TipoActividad
import kotlinx.coroutines.flow.Flow

/** Qué pasa cuando un estudiante intenta empezar una actividad. */
sealed class InicioIntento {
    /** Puede empezar: se creó un intento nuevo. */
    data class Nuevo(val intento: IntentoEntity) : InicioIntento()

    /** Tenía un intento sin terminar: lo retoma donde quedó. */
    data class Retomar(val intento: IntentoEntity) : InicioIntento()

    /** La unidad no está asignada a su curso. */
    data object NoAsignada : InicioIntento()

    /** La prueba todavía no abre. */
    data class AunNoDisponible(val desde: Long) : InicioIntento()

    /** La prueba ya cerró. */
    data object Cerrada : InicioIntento()

    /** Ya usó todos sus intentos. */
    data class SinIntentos(val maximo: Int) : InicioIntento()
}

/** El resultado de un intento terminado. */
data class ResultadoIntento(val correctas: Int, val total: Int)

/** Intentos, respuestas, informe por OA y observaciones del docente. */
class ResultadoRepository(
    private val intentoDao: IntentoDao,
    private val respuestaDao: RespuestaDao,
    private val opcionDao: OpcionDao,
    private val preguntaDao: PreguntaDao,
    private val asignacionDao: AsignacionDao,
    private val observacionDao: ObservacionDocenteDao
) {

    // ---------- Estudiante: rendir actividades ----------

    /**
     * Intenta comenzar una actividad. Aplica las reglas de la prueba final:
     * fechas de apertura y cierre, y límite de intentos. Los desafíos no tienen límite.
     *
     * @param ahora la hora actual; es un parámetro para poder probarlo con otras fechas.
     */
    suspend fun iniciarActividad(
        estudianteId: Long,
        cursoId: Long,
        actividad: ActividadEntity,
        ahora: Long = System.currentTimeMillis()
    ): InicioIntento {
        // 1) Si dejó un intento a medias, lo retoma
        intentoDao.obtenerEnCurso(estudianteId, actividad.id)?.let { return InicioIntento.Retomar(it) }

        // 2) La unidad tiene que estar asignada a su curso
        val asignacion = asignacionDao.obtener(cursoId, actividad.unidadId)
            ?: return InicioIntento.NoAsignada

        val realizados = intentoDao.contarIntentos(estudianteId, actividad.id)

        // 3) Reglas exclusivas de la prueba final
        if (actividad.tipo == TipoActividad.PRUEBA_FINAL) {
            asignacion.fechaInicioPrueba?.let { inicio ->
                if (ahora < inicio) return InicioIntento.AunNoDisponible(inicio)
            }
            asignacion.fechaFinPrueba?.let { fin ->
                if (ahora > fin) return InicioIntento.Cerrada
            }
            if (realizados >= asignacion.intentosMaxPrueba) {
                return InicioIntento.SinIntentos(asignacion.intentosMaxPrueba)
            }
        }

        // 4) Todo en orden: se crea el intento
        val nuevo = IntentoEntity(
            estudianteId = estudianteId,
            actividadId = actividad.id,
            numeroIntento = realizados + 1,
            fechaInicio = ahora
        )
        val id = intentoDao.crear(nuevo)
        return InicioIntento.Nuevo(nuevo.copy(id = id))
    }

    /**
     * Guarda la respuesta apenas el estudiante toca una opción.
     * @return true si fue correcta (para mostrar "¡Bien hecho!").
     */
    suspend fun responder(intentoId: Long, preguntaId: Long, opcionId: Long): Boolean {
        val esCorrecta = opcionDao.obtenerPorId(opcionId)?.esCorrecta ?: false
        respuestaDao.guardar(
            RespuestaEntity(
                intentoId = intentoId,
                preguntaId = preguntaId,
                opcionElegidaId = opcionId,
                esCorrecta = esCorrecta
            )
        )
        return esCorrecta
    }

    /** Cierra el intento y calcula su resultado. */
    suspend fun finalizar(intento: IntentoEntity): ResultadoIntento {
        val correctas = respuestaDao.contarCorrectas(intento.id)
        val total = preguntaDao.contarPorActividad(intento.actividadId)
        intentoDao.finalizar(intentoId = intento.id, correctas = correctas, totalPreguntas = total)
        return ResultadoIntento(correctas, total)
    }

    fun respuestasDelIntento(intentoId: Long): Flow<List<RespuestaEntity>> =
        respuestaDao.obtenerPorIntento(intentoId)

    fun intentosDelEstudiante(estudianteId: Long, actividadId: Long): Flow<List<IntentoEntity>> =
        intentoDao.obtenerPorEstudiante(estudianteId, actividadId)

    // ---------- Docente: monitoreo e informe ----------

    fun intentosDeActividad(actividadId: Long): Flow<List<IntentoEntity>> =
        intentoDao.obtenerPorActividad(actividadId)

    /** El informe: rendimiento del estudiante por OA (mejor intento de la prueba final). */
    fun rendimientoPorOa(estudianteId: Long, unidadId: Long): Flow<List<RendimientoOa>> =
        respuestaDao.obtenerRendimientoPorOa(estudianteId, unidadId)

    fun observacion(estudianteId: Long, unidadId: Long): Flow<ObservacionDocenteEntity?> =
        observacionDao.obtener(estudianteId, unidadId)

    suspend fun guardarObservacion(estudianteId: Long, unidadId: Long, docenteId: Long, texto: String) {
        observacionDao.guardar(
            ObservacionDocenteEntity(
                estudianteId = estudianteId,
                unidadId = unidadId,
                docenteId = docenteId,
                texto = texto
            )
        )
    }

    suspend fun validarInforme(estudianteId: Long, unidadId: Long) =
        observacionDao.validar(estudianteId, unidadId)
}