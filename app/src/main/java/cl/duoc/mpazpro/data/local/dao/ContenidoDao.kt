package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContenidoDao {

    @Query("SELECT * FROM contenido WHERE oaId = :oaId ORDER BY orden")
    fun obtenerPorOa(oaId: Long): Flow<List<ContenidoEntity>>

    /** El siguiente número de orden libre en un OA (1 si todavía no tiene contenidos). */
    @Query("SELECT COALESCE(MAX(orden), 0) + 1 FROM contenido WHERE oaId = :oaId")
    suspend fun siguienteOrden(oaId: Long): Int

    @Insert
    suspend fun insertar(contenido: ContenidoEntity): Long

    @Update
    suspend fun actualizar(contenido: ContenidoEntity)

    @Delete
    suspend fun eliminar(contenido: ContenidoEntity)
}