package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lo que el estudiante respondió en una pregunta, dentro de un intento.
 */
@Entity(
    tableName = "respuesta",
    foreignKeys = [
        ForeignKey(
            entity = IntentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["intentoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PreguntaEntity::class,
            parentColumns = ["id"],
            childColumns = ["preguntaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OpcionEntity::class,
            parentColumns = ["id"],
            childColumns = ["opcionElegidaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["intentoId", "preguntaId"], unique = true),
        Index("preguntaId"),
        Index("opcionElegidaId")
    ]
)
data class RespuestaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val intentoId: Long,
    val preguntaId: Long,
    val opcionElegidaId: Long?,
    val esCorrecta: Boolean,
    val fechaRespuesta: Long = System.currentTimeMillis()
)