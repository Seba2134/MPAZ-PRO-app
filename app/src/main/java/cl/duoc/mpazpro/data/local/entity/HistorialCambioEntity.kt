package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.mpazpro.domain.model.EstadoContenido

/** Registro de cada cambio de estado de una unidad: quién, cuándo y de qué estado a cuál. */
@Entity(
    tableName = "historial_cambio",
    foreignKeys = [
        ForeignKey(
            entity = UnidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["unidadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("unidadId"), Index("usuarioId")]
)
data class HistorialCambioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    val estadoAnterior: EstadoContenido?,  // null cuando la unidad se acaba de crear
    val estadoNuevo: EstadoContenido,
    val usuarioId: Long?,                  // quien hizo el cambio
    val comentario: String? = null,        // ej: "Falta el audio del OA 2"
    val fecha: Long = System.currentTimeMillis()
)