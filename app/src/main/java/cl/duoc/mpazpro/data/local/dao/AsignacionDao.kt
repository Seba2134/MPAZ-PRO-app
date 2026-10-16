package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.AsignacionEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignacionDao {

    // ---------- Para el Docente ----------

    /**
     * Unidades que el docente PUEDE asignar en una asignatura:
     * solo las que UTP ya autorizó (o publicó).
     */
    @Query(
        """
        SELECT * FROM unidad
        WHERE asignaturaId = :asignaturaId
          AND estado IN ('AUTORIZADO', 'PUBLICADO')
        ORDER BY orden
        """
    )
    fun obtenerUnidadesDisponibles(asignaturaId: Long): Flow<List<UnidadEntity>>

    /** Las asignaciones de un curso (para ver qué ya se asignó). */
    @Query("SELECT * FROM asignacion WHERE cursoId = :cursoId ORDER BY fechaAsignacion DESC")
    fun obtenerPorCurso(cursoId: Long): Flow<List<AsignacionEntity>>

    /** Una asignación puntual: la usa la prueba para leer intentos y fechas. */
    @Query("SELECT * FROM asignacion WHERE cursoId = :cursoId AND unidadId = :unidadId")
    suspend fun obtener(cursoId: Long, unidadId: Long): AsignacionEntity?

    /** Asigna una unidad a un curso. Falla si ya estaba asignada (índice único). */
    @Insert
    suspend fun asignar(asignacion: AsignacionEntity): Long

    /** Cambia la configuración de la prueba (intentos y fechas). */
    @Update
    suspend fun actualizar(asignacion: AsignacionEntity)

    @Delete
    suspend fun quitar(asignacion: AsignacionEntity)

    // ---------- Para el Estudiante ----------

    /** Las asignaturas que el estudiante ve: las que tienen al menos una unidad asignada y publicada. */
    @Query(
        """
        SELECT DISTINCT asig.* FROM asignatura asig
        JOIN unidad u     ON u.asignaturaId = asig.id
        JOIN asignacion a ON a.unidadId = u.id
        WHERE a.cursoId = :cursoId
          AND u.estado = 'PUBLICADO'
        ORDER BY asig.id
        """
    )
    fun obtenerAsignaturasDelEstudiante(cursoId: Long): Flow<List<AsignaturaEntity>>

    /** Las unidades de una asignatura que el estudiante puede abrir. */
    @Query(
        """
        SELECT u.* FROM unidad u
        JOIN asignacion a ON a.unidadId = u.id
        WHERE a.cursoId = :cursoId
          AND u.asignaturaId = :asignaturaId
          AND u.estado = 'PUBLICADO'
        ORDER BY u.orden
        """
    )
    fun obtenerUnidadesDelEstudiante(cursoId: Long, asignaturaId: Long): Flow<List<UnidadEntity>>
}