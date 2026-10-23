package cl.duoc.mpazpro.data.local.relation

/**
 * Una unidad que el estudiante tiene por cursar: junta datos de
 * asignacion + unidad + asignatura. No es una tabla.
 */
data class UnidadEstudiante(
    val asignaturaId: Long,
    val asignaturaNombre: String,
    val colorHex: String,
    val unidadId: Long,
    val unidadTitulo: String,
    val instrucciones: String,
    val intentosMaxPrueba: Int,
    val fechaInicioPrueba: Long?,
    val fechaFinPrueba: Long?
)