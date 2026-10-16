package cl.duoc.mpazpro.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity

/** Una pregunta junto con todas sus alternativas. No es una tabla: es el resultado de una consulta. */
data class PreguntaConOpciones(
    //Embedded pone todos los campos de la pregunta "adentro" de este objeto
    @Embedded val pregunta: PreguntaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "preguntaId"
    )
    val opciones: List<OpcionEntity>
)