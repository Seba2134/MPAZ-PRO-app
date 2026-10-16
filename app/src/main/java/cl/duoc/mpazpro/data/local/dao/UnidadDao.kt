package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDao {

    @Query("SELECT * FROM unidad WHERE asignaturaId = :asignaturaId ORDER BY orden")
    fun obtenerPorAsignatura(asignaturaId: Long): Flow<List<UnidadEntity>>

    @Insert
    suspend fun insertar(unidad: UnidadEntity): Long

    @Update
    suspend fun actualizar(unidad: UnidadEntity)
}