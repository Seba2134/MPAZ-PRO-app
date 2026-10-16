package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
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

    @Insert
    suspend fun insertar(actividad: ActividadEntity): Long

    @Update
    suspend fun actualizar(actividad: ActividadEntity)
}