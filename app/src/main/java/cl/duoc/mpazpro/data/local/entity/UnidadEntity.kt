package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.mpazpro.domain.model.EstadoContenido

@Entity(
    tableName = "unidad",
    foreignKeys = [
        ForeignKey(
            entity = AsignaturaEntity::class,
            parentColumns = ["id"],
            childColumns = ["asignaturaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("asignaturaId")]
)
data class UnidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val asignaturaId: Long,
    val titulo: String,
    val orden: Int,
    val estado: EstadoContenido = EstadoContenido.BORRADOR
)