package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.domain.model.EstadoContenido
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDao {

    @Query("SELECT * FROM unidad WHERE asignaturaId = :asignaturaId ORDER BY orden")
    fun obtenerPorAsignatura(asignaturaId: Long): Flow<List<UnidadEntity>>

    /** Para UTP: por ejemplo, todas las unidades "En revisión". */
    @Query("SELECT * FROM unidad WHERE estado = :estado ORDER BY asignaturaId, orden")
    fun obtenerPorEstado(estado: EstadoContenido): Flow<List<UnidadEntity>>

    @Query("SELECT * FROM unidad WHERE id = :id")
    suspend fun obtenerPorId(id: Long): UnidadEntity?

    @Insert
    suspend fun insertar(unidad: UnidadEntity): Long

    @Update
    suspend fun actualizar(unidad: UnidadEntity)
}