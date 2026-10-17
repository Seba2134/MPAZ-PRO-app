package cl.duoc.mpazpro.data.local.relation

/**
 * Una fila del informe: cómo le fue a un estudiante en un OA.
 * Ej: OA 2 → 3 correctas de 5.
 */
data class RendimientoOa(
    val oaId: Long,
    val codigo: String,
    val descripcion: String,
    val correctas: Int,
    val total: Int
) {
    /** Porcentaje de logro, de 0 a 100. */
    val porcentaje: Int
        get() = if (total == 0) 0 else (correctas * 100) / total
}