package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cada vez que un estudiante comienza una actividad (desafío o prueba final).
 * fechaFin = null significa que el intento sigue en curso.
 */
@Entity(
    tableName = "intento",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["estudianteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActividadEntity::class,
            parentColumns = ["id"],
            childColumns = ["actividadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["estudianteId", "actividadId", "numeroIntento"], unique = true),
        Index("actividadId")
    ]
)
data class IntentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val estudianteId: Long,
    val actividadId: Long,
    val numeroIntento: Int,                          // 1, 2, 3...
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaFin: Long? = null,                      // null = en curso
    val correctas: Int = 0,
    val totalPreguntas: Int = 0
)