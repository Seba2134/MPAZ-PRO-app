package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.mpazpro.domain.model.TipoPregunta

@Entity(
    tableName = "pregunta",
    foreignKeys = [
        ForeignKey(
            entity = ActividadEntity::class,
            parentColumns = ["id"],
            childColumns = ["actividadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ObjetivoAprendizajeEntity::class,
            parentColumns = ["id"],
            childColumns = ["oaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("actividadId"), Index("oaId")]
)
data class PreguntaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actividadId: Long,
    val oaId: Long,
    val enunciado: String,             // ej: "Cual de estos animales tiene plumas?"
    val tipo: TipoPregunta = TipoPregunta.SELECCION,
    val recursoAudio: String? = null,  // el enunciado leído en voz alta
    val recursoImagen: String? = null,
    val orden: Int
)