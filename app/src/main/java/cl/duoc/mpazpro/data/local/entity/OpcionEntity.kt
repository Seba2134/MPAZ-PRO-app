package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "opcion",
    foreignKeys = [
        ForeignKey(
            entity = PreguntaEntity::class,
            parentColumns = ["id"],
            childColumns = ["preguntaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("preguntaId")]
)
data class OpcionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val preguntaId: Long,
    val texto: String,                 // ej: "Pinguino"
    val recursoImagen: String? = null, // foto del pinguino
    val esCorrecta: Boolean = false,
    val orden: Int
)