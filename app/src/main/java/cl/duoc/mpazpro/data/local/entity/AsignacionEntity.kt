package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una unidad asignada por un docente a un curso, con la configuración de su prueba final.
 * Las fechas se guardan en milisegundos (System.currentTimeMillis()).
 */
@Entity(
    tableName = "asignacion",
    foreignKeys = [
        ForeignKey(
            entity = CursoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
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
            childColumns = ["asignadoPorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["cursoId", "unidadId"], unique = true),
        Index("unidadId"),
        Index("asignadoPorId")
    ]
)
data class AsignacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cursoId: Long,
    val unidadId: Long,
    val asignadoPorId: Long?,             // el docente que la asignó
    val fechaAsignacion: Long = System.currentTimeMillis(),

    //Configuración de la prueba final
    val instrucciones: String = "",       // mensaje del docente para sus estudiantes
    val intentosMaxPrueba: Int = 2,
    val fechaInicioPrueba: Long? = null,  // null = disponible desde ya
    val fechaFinPrueba: Long? = null,     // null = sin fecha límite
    val mostrarRespuestas: Boolean = true // ¿el estudiante ve las respuestas correctas al terminar?
)