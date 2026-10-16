package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.mpazpro.domain.model.TipoActividad

@Entity(
    tableName = "actividad",
    foreignKeys = [
        ForeignKey(
            entity = UnidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["unidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unidadId")]
)
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    val titulo: String,       // "Desafío 1: ¿Qué animal es?"
    val instruccion: String,  // "Escucha y toca la respuesta correcta"
    val tipo: TipoActividad,
    val orden: Int
)