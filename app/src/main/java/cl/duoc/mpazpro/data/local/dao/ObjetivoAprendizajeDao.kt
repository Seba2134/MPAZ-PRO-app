package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjetivoAprendizajeDao {

    @Query("SELECT * FROM objetivo_aprendizaje WHERE unidadId = :unidadId ORDER BY codigo")
    fun obtenerPorUnidad(unidadId: Long): Flow<List<ObjetivoAprendizajeEntity>>

    @Query("SELECT * FROM objetivo_aprendizaje WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ObjetivoAprendizajeEntity?

    @Insert
    suspend fun insertar(objetivo: ObjetivoAprendizajeEntity): Long

    @Update
    suspend fun actualizar(objetivo: ObjetivoAprendizajeEntity)
}