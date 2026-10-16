package cl.duoc.mpazpro.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity

/** Un curso junto con todos sus estudiantes. No es una tabla. */
data class CursoConEstudiantes(
    @Embedded val curso: CursoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "cursoId"
    )
    val estudiantes: List<UsuarioEntity>
)