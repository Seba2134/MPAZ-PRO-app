package cl.duoc.mpazpro.resultados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Lo que manda la app para ACTUALIZAR una observación (PUT).
 * Si el texto cambia, la validación se pierde; para validar se manda el mismo texto con validado = true.
 */
public record ObservacionActualizacion(
    @NotBlank(message = "Escribe una observación") @Size(max = 500, message = "Máximo 500 caracteres") String texto,
    @NotNull Boolean validado
) {
}
