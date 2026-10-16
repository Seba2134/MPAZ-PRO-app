package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignaturaDao {

    @Query("SELECT * FROM asignatura ORDER BY id")
    fun obtenerTodas(): Flow<List<AsignaturaEntity>>

    @Insert
    suspend fun insertar(asignatura: AsignaturaEntity): Long

    @Query("SELECT COUNT(*) FROM asignatura")
    suspend fun contar(): Int
}