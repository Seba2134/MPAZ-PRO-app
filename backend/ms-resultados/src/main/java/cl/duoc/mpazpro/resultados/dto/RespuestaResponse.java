package cl.duoc.mpazpro.resultados.dto;

import cl.duoc.mpazpro.resultados.model.Respuesta;

/** Una respuesta guardada, tal como sale por la API. */
public record RespuestaResponse(
    Long preguntaId,
    String enunciado,
    String oaCodigo,
    String oaDescripcion,
    boolean esCorrecta
) {
    public static RespuestaResponse desde(Respuesta respuesta) {
        return new RespuestaResponse(
            respuesta.getPreguntaId(),
            respuesta.getEnunciado(),
            respuesta.getOaCodigo(),
            respuesta.getOaDescripcion(),
            respuesta.isEsCorrecta()
        );
    }
}
