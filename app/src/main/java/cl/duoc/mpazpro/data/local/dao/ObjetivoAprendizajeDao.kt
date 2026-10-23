package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.relation.OaConUnidad
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjetivoAprendizajeDao {

    @Query("SELECT * FROM objetivo_aprendizaje WHERE unidadId = :unidadId ORDER BY codigo")
    fun obtenerPorUnidad(unidadId: Long): Flow<List<ObjetivoAprendizajeEntity>>

    /** Todos los OA con su unidad y asignatura (para el selector de "Crear contenido"). */
    @Query(
        """
        SELECT oa.id AS id,
               u.asignaturaId AS asignaturaId,
               oa.codigo AS codigo,
               oa.descripcion AS descripcion,
               u.titulo AS unidadTitulo
        FROM objetivo_aprendizaje oa
        JOIN unidad u ON u.id = oa.unidadId
        ORDER BY u.asignaturaId, u.orden, oa.codigo
        """
    )
    fun obtenerTodosConUnidad(): Flow<List<OaConUnidad>>

    @Query("SELECT * FROM objetivo_aprendizaje WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ObjetivoAprendizajeEntity?

    @Insert
    suspend fun insertar(objetivo: ObjetivoAprendizajeEntity): Long

    @Update
    suspend fun actualizar(objetivo: ObjetivoAprendizajeEntity)
}