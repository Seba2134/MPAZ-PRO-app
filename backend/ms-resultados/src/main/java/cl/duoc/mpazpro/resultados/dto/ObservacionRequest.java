package cl.duoc.mpazpro.resultados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Lo que manda la app para CREAR una observación (POST). Nace sin validar. */
public record ObservacionRequest(
    @NotBlank @Size(max = 50) String estudianteNombreUsuario,
    @NotBlank @Size(max = 100) String unidadTitulo,
    @NotBlank(message = "Escribe una observación") @Size(max = 500, message = "Máximo 500 caracteres") String texto
) {
}
