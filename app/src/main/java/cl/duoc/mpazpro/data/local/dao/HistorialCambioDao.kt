package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.mpazpro.data.local.entity.HistorialCambioEntity
import cl.duoc.mpazpro.domain.model.EstadoContenido
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HistorialCambioDao {

    /** La historia completa de una unidad, de la más reciente a la más antigua. */
    @Query("SELECT * FROM historial_cambio WHERE unidadId = :unidadId ORDER BY fecha DESC")
    abstract fun obtenerPorUnidad(unidadId: Long): Flow<List<HistorialCambioEntity>>

    @Insert
    protected abstract suspend fun registrar(cambio: HistorialCambioEntity)

    @Query("UPDATE unidad SET estado = :nuevo WHERE id = :unidadId")
    protected abstract suspend fun actualizarEstadoUnidad(unidadId: Long, nuevo: EstadoContenido)

    /**
     * Cambia el estado de una unidad Y registra el cambio, en una sola transacción.
     * O se hacen las dos cosas, o ninguna.
     */
    @Transaction
    open suspend fun cambiarEstado(
        unidadId: Long,
        anterior: EstadoContenido?,
        nuevo: EstadoContenido,
        usuarioId: Long,
        comentario: String? = null
    ) {
        actualizarEstadoUnidad(unidadId, nuevo)
        registrar(
            HistorialCambioEntity(
                unidadId = unidadId,
                estadoAnterior = anterior,
                estadoNuevo = nuevo,
                usuarioId = usuarioId,
                comentario = comentario
            )
        )
    }
}