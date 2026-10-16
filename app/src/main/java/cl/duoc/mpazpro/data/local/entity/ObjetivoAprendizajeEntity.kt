package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "objetivo_aprendizaje",
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
data class ObjetivoAprendizajeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    val codigo: String,      // ej: "OA 2"
    val descripcion: String  // ej: "Clasificar animales según su cubierta corporal"
)