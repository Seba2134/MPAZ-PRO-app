package cl.duoc.mpazpro.resultados.dto;

/** Una fila del informe: cómo le fue al estudiante en un OA (porcentaje de 0 a 100). */
public record RendimientoOaResponse(
    String oaCodigo,
    String oaDescripcion,
    int correctas,
    int total,
    int porcentaje
) {
}
