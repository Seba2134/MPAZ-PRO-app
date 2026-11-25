package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.relation.ActividadResumen
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    /** Todas las actividades de una unidad: desafíos y prueba final. */
    @Query("SELECT * FROM actividad WHERE unidadId = :unidadId ORDER BY orden")
    fun obtenerPorUnidad(unidadId: Long): Flow<List<ActividadEntity>>

    /** Solo los desafios de practica (sin la prueba). */
    @Query("SELECT * FROM actividad WHERE unidadId = :unidadId AND tipo = 'DESAFIO' ORDER BY orden")
    fun obtenerDesafios(unidadId: Long): Flow<List<ActividadEntity>>

    /** La prueba final de la unidad (puede no existir todavía). */
    @Query("SELECT * FROM actividad WHERE unidadId = :unidadId AND tipo = 'PRUEBA_FINAL' LIMIT 1")
    suspend fun obtenerPruebaFinal(unidadId: Long): ActividadEntity?

    @Query("SELECT * FROM actividad WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ActividadEntity?

    /** Las actividades de la unidad con su cantidad de preguntas (para la pantalla de UTP). */
    @Query(
        """
        SELECT a.id, a.titulo, a.tipo, a.orden,
               (SELECT COUNT(*) FROM pregunta p WHERE p.actividadId = a.id) AS cantidadPreguntas
        FROM actividad a
        WHERE a.unidadId = :unidadId
        ORDER BY a.orden
        """
    )
    fun obtenerResumenPorUnidad(unidadId: Long): Flow<List<ActividadResumen>>

    /** El siguiente número de orden libre en una unidad (1 si todavía no tiene actividades). */
    @Query("SELECT COALESCE(MAX(orden), 0) + 1 FROM actividad WHERE unidadId = :unidadId")
    suspend fun siguienteOrden(unidadId: Long): Int

    @Insert
    suspend fun insertar(actividad: ActividadEntity): Long

    @Update
    suspend fun actualizar(actividad: ActividadEntity)
}