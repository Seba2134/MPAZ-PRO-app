package cl.duoc.mpazpro.data.local.relation

/**
 * Una prueba que el docente puede configurar: junta datos de
 * asignacion + curso + unidad + asignatura. No es una tabla.
 */
data class PruebaDocente(
    val asignacionId: Long,
    val cursoNombre: String,
    val unidadTitulo: String,
    val asignaturaNombre: String,
    val colorHex: String,
    val intentosMaxPrueba: Int,
    val fechaInicioPrueba: Long?,
    val fechaFinPrueba: Long?
)