package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.dao.ActividadDao
import cl.duoc.mpazpro.data.local.dao.AsignaturaDao
import cl.duoc.mpazpro.data.local.dao.ContenidoDao
import cl.duoc.mpazpro.data.local.dao.HistorialCambioDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoAprendizajeDao
import cl.duoc.mpazpro.data.local.dao.OpcionDao
import cl.duoc.mpazpro.data.local.dao.PreguntaDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.dao.UsuarioDao
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.HistorialCambioEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.data.local.relation.ActividadResumen
import cl.duoc.mpazpro.data.local.relation.HistorialConUsuario
import cl.duoc.mpazpro.data.local.relation.OaConUnidad
import cl.duoc.mpazpro.data.local.relation.PreguntaConOpciones
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.model.TipoActividad
import kotlinx.coroutines.flow.Flow

/**
 * Maneja todo el contenido pedagógico: lo crea Jefatura UTP y lo leen docentes y estudiantes.
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
    private val historialDao: HistorialCambioDao,
    private val usuarioDao: UsuarioDao
) {

    // ---------- Lectura ----------

    fun asignaturas(): Flow<List<AsignaturaEntity>> = asignaturaDao.obtenerTodas()

    fun unidades(asignaturaId: Long): Flow<List<UnidadEntity>> = unidadDao.obtenerPorAsignatura(asignaturaId)

    fun unidadesPorEstado(estado: EstadoContenido): Flow<List<UnidadEntity>> = unidadDao.obtenerPorEstado(estado)

    suspend fun unidad(unidadId: Long): UnidadEntity? = unidadDao.obtenerPorId(unidadId)

    fun objetivos(unidadId: Long): Flow<List<ObjetivoAprendizajeEntity>> = objetivoDao.obtenerPorUnidad(unidadId)

    /** Todos los OA con su unidad (para el selector de "Crear contenido"). */
    fun objetivosConUnidad(): Flow<List<OaConUnidad>> = objetivoDao.obtenerTodosConUnidad()

    fun contenidos(oaId: Long): Flow<List<ContenidoEntity>> = contenidoDao.obtenerPorOa(oaId)

    /** Todos los contenidos de la unidad (de todos sus OA) en una sola consulta. */
    fun contenidosDeUnidad(unidadId: Long): Flow<List<ContenidoEntity>> = contenidoDao.obtenerPorUnidad(unidadId)

    /** El siguiente número de orden libre para un contenido nuevo en ese OA. */
    suspend fun siguienteOrden(oaId: Long): Int = contenidoDao.siguienteOrden(oaId)

    fun actividades(unidadId: Long): Flow<List<ActividadEntity>> = actividadDao.obtenerPorUnidad(unidadId)

    suspend fun actividad(actividadId: Long): ActividadEntity? = actividadDao.obtenerPorId(actividadId)

    /** Los desafíos de práctica de la unidad (sin la prueba final). */
    fun desafios(unidadId: Long): Flow<List<ActividadEntity>> = actividadDao.obtenerDesafios(unidadId)

    /** La prueba final de la unidad, o null si todavía no tiene. */
    suspend fun pruebaFinal(unidadId: Long): ActividadEntity? = actividadDao.obtenerPruebaFinal(unidadId)

    fun preguntasConOpciones(actividadId: Long): Flow<List<PreguntaConOpciones>> =
        preguntaDao.obtenerConOpciones(actividadId)

    fun historial(unidadId: Long): Flow<List<HistorialCambioEntity>> = historialDao.obtenerPorUnidad(unidadId)

    /** El historial con el nombre de quien hizo cada cambio. */
    fun historialConUsuario(unidadId: Long): Flow<List<HistorialConUsuario>> = historialDao.obtenerConUsuario(unidadId)

    /** La unidad, actualizada cada vez que cambia (estado, título…). */
    fun observarUnidad(unidadId: Long): Flow<UnidadEntity?> = unidadDao.observarPorId(unidadId)

    /** Las actividades de la unidad con su cantidad de preguntas. */
    fun resumenActividades(unidadId: Long): Flow<List<ActividadResumen>> = actividadDao.obtenerResumenPorUnidad(unidadId)

    // ---------- Creación (Jefatura UTP) ----------
    // Todas las creaciones y cambios de estado verifican que quien lo hace sea Jefatura UTP.
    // Si no lo es, no se hace nada y se devuelve -1 (o false).

    /** El siguiente número de orden libre para una unidad nueva en esa asignatura. */
    suspend fun siguienteOrdenUnidad(asignaturaId: Long): Int = unidadDao.siguienteOrden(asignaturaId)

    /** Crea una unidad en Borrador y deja registrado quién la creó. */
    suspend fun crearUnidad(asignaturaId: Long, titulo: String, orden: Int, utpId: Long): Long {
        if (!esJefaturaUtp(utpId)) return NO_CREADO
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

    /** Agrega un OA a una unidad. Solo mientras la unidad está en Borrador. */
    suspend fun crearObjetivo(objetivo: ObjetivoAprendizajeEntity, utpId: Long): Long {
        if (!esJefaturaUtp(utpId) || !estaEnBorrador(objetivo.unidadId)) return NO_CREADO
        return objetivoDao.insertar(objetivo)
    }

    suspend fun crearContenido(contenido: ContenidoEntity): Long = contenidoDao.insertar(contenido)

    /** El siguiente número de orden libre para una actividad nueva en esa unidad. */
    suspend fun siguienteOrdenActividad(unidadId: Long): Int = actividadDao.siguienteOrden(unidadId)

    /** Cuántas preguntas tiene una actividad (para el orden de la siguiente). */
    suspend fun cantidadPreguntas(actividadId: Long): Int = preguntaDao.contarPorActividad(actividadId)

    /**
     * Crea un desafío o la prueba final de una unidad en Borrador.
     * Una unidad tiene a lo más una prueba final.
     */
    suspend fun crearActividad(actividad: ActividadEntity, utpId: Long): Long {
        if (!esJefaturaUtp(utpId) || !estaEnBorrador(actividad.unidadId)) return NO_CREADO
        if (actividad.tipo == TipoActividad.PRUEBA_FINAL && actividadDao.obtenerPruebaFinal(actividad.unidadId) != null) {
            return NO_CREADO
        }
        return actividadDao.insertar(actividad)
    }

    /** Crea una pregunta y todas sus opciones de una vez (solo si la unidad de la actividad está en Borrador). */
    suspend fun crearPreguntaConOpciones(pregunta: PreguntaEntity, opciones: List<OpcionEntity>, utpId: Long): Long {
        if (!esJefaturaUtp(utpId) || opciones.isEmpty()) return NO_CREADO
        val actividad = actividadDao.obtenerPorId(pregunta.actividadId) ?: return NO_CREADO
        val oa = objetivoDao.obtenerPorId(pregunta.oaId) ?: return NO_CREADO
        if (oa.unidadId != actividad.unidadId || !estaEnBorrador(actividad.unidadId)) return NO_CREADO

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
        if (!esJefaturaUtp(usuarioId)) return false
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

    // ---------- Ayudantes internos ----------

    private suspend fun esJefaturaUtp(usuarioId: Long): Boolean =
        usuarioDao.obtenerPorId(usuarioId)?.rol == Rol.JEFATURA_UTP

    private suspend fun estaEnBorrador(unidadId: Long): Boolean =
        unidadDao.obtenerPorId(unidadId)?.estado == EstadoContenido.BORRADOR

    companion object {
        /** Lo que devuelven las funciones de creación cuando no se creó nada. */
        const val NO_CREADO = -1L
    }
}