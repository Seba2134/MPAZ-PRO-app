package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.mpazpro.data.local.entity.IntentoEntity
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