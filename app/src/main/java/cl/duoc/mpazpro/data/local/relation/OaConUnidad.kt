package cl.duoc.mpazpro.data.local.relation

/**
 * Un objetivo de aprendizaje con su unidad y asignatura, para el selector de "Crear contenido".
 * No es una tabla: es el resultado de una consulta.
 */
data class OaConUnidad(
    val id: Long,
    val asignaturaId: Long,
    val codigo: String,
    val descripcion: String,
    val unidadTitulo: String
)