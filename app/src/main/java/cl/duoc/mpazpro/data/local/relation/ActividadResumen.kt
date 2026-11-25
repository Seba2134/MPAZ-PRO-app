package cl.duoc.mpazpro.data.local.relation

import cl.duoc.mpazpro.domain.model.TipoActividad

/** Una actividad de la unidad con cuántas preguntas tiene (para la lista de UTP). No es una tabla. */
data class ActividadResumen(
    val id: Long,
    val titulo: String,
    val tipo: TipoActividad,
    val orden: Int,
    val cantidadPreguntas: Int
)
