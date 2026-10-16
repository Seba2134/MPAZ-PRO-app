package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Carga académica: qué docente imparte qué asignatura en qué curso.
 * La asigna Jefatura UTP. El profesor jefe NO necesita filas aquí:
 * su acceso a todas las asignaturas del curso sale de CursoEntity.profesorJefeId.
 */
@Entity(
    tableName = "docente_asignatura",
    primaryKeys = ["cursoId", "asignaturaId"],
    foreignKeys = [
        ForeignKey(
            entity = CursoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AsignaturaEntity::class,
            parentColumns = ["id"],
            childColumns = ["asignaturaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["docenteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("asignaturaId"), Index("docenteId")]
)
data class DocenteAsignaturaEntity(
    val cursoId: Long,
    val asignaturaId: Long,
    val docenteId: Long
)