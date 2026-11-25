package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.mpazpro.data.local.entity.IntentoEntity
import cl.duoc.mpazpro.data.local.relation.IntentoParaEnviar
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentoDao {

    /** Cuántos intentos lleva el estudiante en una actividad (para el límite). */
    @Query("SELECT COUNT(*) FROM intento WHERE estudianteId = :estudianteId AND actividadId = :actividadId")
    suspend fun contarIntentos(estudianteId: Long, actividadId: Long): Int

    /** El intento sin terminar, si existe (para retomar si cerró la app). */
    @Query(
        """
        SELECT * FROM intento
        WHERE estudianteId = :estudianteId AND actividadId = :actividadId AND fechaFin IS NULL
        ORDER BY numeroIntento DESC LIMIT 1
        """
    )
    suspend fun obtenerEnCurso(estudianteId: Long, actividadId: Long): IntentoEntity?

    /** Todos los intentos de un estudiante en una actividad (para mostrar su historial). */
    @Query("SELECT * FROM intento WHERE estudianteId = :estudianteId AND actividadId = :actividadId ORDER BY numeroIntento")
    fun obtenerPorEstudiante(estudianteId: Long, actividadId: Long): Flow<List<IntentoEntity>>

    /** Todos los intentos de una actividad (para el monitoreo del docente). */
    @Query("SELECT * FROM intento WHERE actividadId = :actividadId ORDER BY estudianteId, numeroIntento")
    fun obtenerPorActividad(actividadId: Long): Flow<List<IntentoEntity>>

    /** Las actividades que el estudiante ya terminó al menos una vez (para marcar «Hecho» y el avance). */
    @Query("SELECT DISTINCT actividadId FROM intento WHERE estudianteId = :estudianteId AND fechaFin IS NOT NULL")
    fun obtenerActividadesTerminadas(estudianteId: Long): Flow<List<Long>>

    /** El mejor intento terminado (mismo criterio que el informe: más correctas y, si empatan, el más reciente). */
    @Query(
        """
        SELECT * FROM intento
        WHERE estudianteId = :estudianteId AND actividadId = :actividadId AND fechaFin IS NOT NULL
        ORDER BY correctas DESC, numeroIntento DESC
        LIMIT 1
        """
    )
    fun obtenerMejorIntento(estudianteId: Long, actividadId: Long): Flow<IntentoEntity?>

    // ---------- Envío al servidor ----------

    /** Los intentos terminados que todavía no se envían, con todos los datos que pide el servidor. */
    @Query(
        """
        SELECT i.id               AS intentoId,
               u.nombreUsuario    AS estudianteNombreUsuario,
               u.nombre           AS estudianteNombre,
               COALESCE(c.nombre, 'Sin curso') AS cursoNombre,
               asig.nombre        AS asignaturaNombre,
               un.titulo          AS unidadTitulo,
               a.id               AS actividadId,
               a.titulo           AS actividadTitulo,
               a.tipo             AS tipo,
               i.numeroIntento    AS numeroIntento,
               i.fechaInicio      AS fechaInicio,
               i.fechaFin         AS fechaFin,
               i.correctas        AS correctas,
               i.totalPreguntas   AS totalPreguntas
        FROM intento i
        JOIN usuario u       ON u.id = i.estudianteId
        LEFT JOIN curso c    ON c.id = u.cursoId
        JOIN actividad a     ON a.id = i.actividadId
        JOIN unidad un       ON un.id = a.unidadId
        JOIN asignatura asig ON asig.id = un.asignaturaId
        WHERE i.fechaFin IS NOT NULL AND i.sincronizado = 0
        ORDER BY i.fechaFin
        """
    )
    suspend fun obtenerPendientes(): List<IntentoParaEnviar>

    /** Cuántos resultados quedan por enviar (para la tarjeta del docente). */
    @Query("SELECT COUNT(*) FROM intento WHERE fechaFin IS NOT NULL AND sincronizado = 0")
    fun contarPendientes(): Flow<Int>

    @Query("UPDATE intento SET sincronizado = 1 WHERE id = :intentoId")
    suspend fun marcarSincronizado(intentoId: Long)

    @Insert
    suspend fun crear(intento: IntentoEntity): Long

    /** Cierra el intento con su resultado final. */
    @Query(
        """
        UPDATE intento
        SET fechaFin = :fechaFin, correctas = :correctas, totalPreguntas = :totalPreguntas
        WHERE id = :intentoId
        """
    )
    suspend fun finalizar(intentoId: Long, correctas: Int, totalPreguntas: Int, fechaFin: Long = System.currentTimeMillis())
}