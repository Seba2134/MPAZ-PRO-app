package cl.duoc.mpazpro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Una respuesta dentro de un intento enviado por la app. */
public record RespuestaRequest(
    @NotNull Long preguntaId,
    @NotBlank @Size(max = 300) String enunciado,
    @NotBlank @Size(max = 20) String oaCodigo,
    @Size(max = 300) String oaDescripcion,
    @NotNull Boolean esCorrecta
) {
}
