package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * La observación que el docente escribe en el informe de un estudiante, para una unidad.
 * El docente la "valida" cuando el informe está listo.
 */
@Entity(
    tableName = "observacion_docente",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["estudianteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["unidadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["docenteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["estudianteId", "unidadId"], unique = true),
        Index("unidadId"),
        Index("docenteId")
    ]
)
data class ObservacionDocenteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val estudianteId: Long,
    val unidadId: Long,
    val docenteId: Long?,          // quién escribió la observación
    val texto: String,
    val validado: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)