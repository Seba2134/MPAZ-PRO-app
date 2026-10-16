package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.mpazpro.domain.model.Licencia
import cl.duoc.mpazpro.domain.model.TipoContenido

@Entity(
    tableName = "contenido",
    foreignKeys = [
        ForeignKey(
            entity = ObjetivoAprendizajeEntity::class,
            parentColumns = ["id"],
            childColumns = ["oaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("oaId")]
)
data class ContenidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oaId: Long,
    val tipo: TipoContenido,
    val texto: String? = null,    // el texto a leer, o el pie de una imagen
    val recurso: String? = null,  // nombre del archivo: "lenguaje_u1_cuento"
    val orden: Int,
    val autor: String,
    val licencia: Licencia
)