package cl.duoc.mpazpro.api.dto;

import cl.duoc.mpazpro.api.model.TipoActividad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Un intento terminado que la app envía al servidor.
 * La regla «correctas no puede ser mayor que total» se revisa en IntentoService.
 */
public record IntentoRequest(
    @NotBlank @Size(max = 50) String estudianteNombreUsuario,
    @NotBlank @Size(max = 100) String estudianteNombre,
    @NotBlank @Size(max = 50) String cursoNombre,
    @NotBlank @Size(max = 100) String asignaturaNombre,
    @NotBlank @Size(max = 100) String unidadTitulo,
    @NotNull Long actividadId,
    @NotBlank @Size(max = 100) String actividadTitulo,
    @NotNull TipoActividad tipo,
    @NotNull @Min(1) Integer numeroIntento,
    @NotNull Long fechaInicio,
    @NotNull Long fechaFin,
    @NotNull @Min(0) Integer correctas,
    @NotNull @Min(0) Integer total,
    @NotNull @Size(max = 100) List<@Valid @NotNull RespuestaRequest> respuestas
) {
}
