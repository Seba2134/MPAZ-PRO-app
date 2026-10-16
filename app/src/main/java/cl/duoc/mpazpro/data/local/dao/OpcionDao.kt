package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.OpcionEntity

@Dao
interface OpcionDao {

    @Query("SELECT * FROM opcion WHERE id = :id")
    suspend fun obtenerPorId(id: Long): OpcionEntity?

    @Insert
    suspend fun insertar(opcion: OpcionEntity): Long

    @Insert
    suspend fun insertarTodas(opciones: List<OpcionEntity>)

    @Update
    suspend fun actualizar(opcion: OpcionEntity)

    @Delete
    suspend fun eliminar(opcion: OpcionEntity)
}