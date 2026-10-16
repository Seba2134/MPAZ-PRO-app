package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.relation.PreguntaConOpciones
import kotlinx.coroutines.flow.Flow

@Dao
interface PreguntaDao {

    /** Lo que usa la pantalla del estudiante: cada pregunta con sus opciones. */
    @Transaction
    @Query("SELECT * FROM pregunta WHERE actividadId = :actividadId ORDER BY orden")
    fun obtenerConOpciones(actividadId: Long): Flow<List<PreguntaConOpciones>>

    @Query("SELECT COUNT(*) FROM pregunta WHERE actividadId = :actividadId")
    suspend fun contarPorActividad(actividadId: Long): Int

    @Insert
    suspend fun insertar(pregunta: PreguntaEntity): Long

    @Update
    suspend fun actualizar(pregunta: PreguntaEntity)
}