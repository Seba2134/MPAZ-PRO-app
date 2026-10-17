package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.dao.ActividadDao
import cl.duoc.mpazpro.data.local.dao.AsignaturaDao
import cl.duoc.mpazpro.data.local.dao.ContenidoDao
import cl.duoc.mpazpro.data.local.dao.HistorialCambioDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoAprendizajeDao
import cl.duoc.mpazpro.data.local.dao.OpcionDao
import cl.duoc.mpazpro.data.local.dao.PreguntaDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.HistorialCambioEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.data.local.relation.PreguntaConOpciones
import cl.duoc.mpazpro.domain.model.EstadoContenido
import kotlinx.coroutines.flow.Flow

/**
 * todo el contenido pedagógico: lo crea Jefatura UTP y lo leen docentes y estudiantes.
 * Incluye el flujo de estados de las unidades, con su historial.
 */
class ContenidoRepository(
    private val asignaturaDao: AsignaturaDao,
    private val unidadDao: UnidadDao,
    private val objetivoDao: ObjetivoAprendizajeDao,
    private val contenidoDao: ContenidoDao,
    private val actividadDao: ActividadDao,
    private val preguntaDao: PreguntaDao,
    private val opcionDao: OpcionDao,
    private val historialDao: HistorialCambioDao
) {

    // ---------- Lectura ----------

    fun asignaturas(): Flow<List<AsignaturaEntity>> = asignaturaDao.obtenerTodas()

    fun unidades(asignaturaId: Long): Flow<List<UnidadEntity>> = unidadDao.obtenerPorAsignatura(asignaturaId)

    fun unidadesPorEstado(estado: EstadoContenido): Flow<List<UnidadEntity>> = unidadDao.obtenerPorEstado(estado)

    suspend fun unidad(unidadId: Long): UnidadEntity? = unidadDao.obtenerPorId(unidadId)

    fun objetivos(unidadId: Long): Flow<List<ObjetivoAprendizajeEntity>> = objetivoDao.obtenerPorUnidad(unidadId)

    fun contenidos(oaId: Long): Flow<List<ContenidoEntity>> = contenidoDao.obtenerPorOa(oaId)

    fun actividades(unidadId: Long): Flow<List<ActividadEntity>> = actividadDao.obtenerPorUnidad(unidadId)

    suspend fun actividad(actividadId: Long): ActividadEntity? = actividadDao.obtenerPorId(actividadId)

    fun preguntasConOpciones(actividadId: Long): Flow<List<PreguntaConOpciones>> =
        preguntaDao.obtenerConOpciones(actividadId)

    fun historial(unidadId: Long): Flow<List<HistorialCambioEntity>> = historialDao.obtenerPorUnidad(unidadId)

    // ---------- Creación (Jefatura UTP) ----------
    /** Crea una unidad en Borrador y deja registrado quién la creó. */
    suspend fun crearUnidad(asignaturaId: Long, titulo: String, orden: Int, utpId: Long): Long {
        val unidadId = unidadDao.insertar(
            UnidadEntity(asignaturaId = asignaturaId, titulo = titulo, orden = orden)
        )
        historialDao.cambiarEstado(
            unidadId = unidadId,
            anterior = null,
            nuevo = EstadoContenido.BORRADOR,
            usuarioId = utpId,
            comentario = "Unidad creada"
        )
        return unidadId
    }

    suspend fun crearObjetivo(objetivo: ObjetivoAprendizajeEntity): Long = objetivoDao.insertar(objetivo)

    suspend fun crearContenido(contenido: ContenidoEntity): Long = contenidoDao.insertar(contenido)

    suspend fun crearActividad(actividad: ActividadEntity): Long = actividadDao.insertar(actividad)

    /** Crea una pregunta y todas sus opciones de una vez. */
    suspend fun crearPreguntaConOpciones(pregunta: PreguntaEntity, opciones: List<OpcionEntity>): Long {
        val preguntaId = preguntaDao.insertar(pregunta)
        opcionDao.insertarTodas(opciones.map { it.copy(preguntaId = preguntaId) })
        return preguntaId
    }

    // ---------- Flujo de estados ----------

    /**
     * Cambia el estado de una unidad si la transición está permitida.
     * @return true si se cambió; false si la unidad no existe o el cambio no es válido.
     */
    suspend fun cambiarEstadoUnidad(
        unidadId: Long,
        nuevo: EstadoContenido,
        usuarioId: Long,
        comentario: String? = null
    ): Boolean {
        val unidad = unidadDao.obtenerPorId(unidadId) ?: return false
        if (!unidad.estado.puedeCambiarA(nuevo)) return false

        historialDao.cambiarEstado(
            unidadId = unidadId,
            anterior = unidad.estado,
            nuevo = nuevo,
            usuarioId = usuarioId,
            comentario = comentario
        )
        return true
    }
}