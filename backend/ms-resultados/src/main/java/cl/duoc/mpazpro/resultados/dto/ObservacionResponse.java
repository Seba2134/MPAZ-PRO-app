package cl.duoc.mpazpro.resultados.dto;

import cl.duoc.mpazpro.resultados.model.Observacion;

/** Una observación guardada, tal como sale por la API. */
public record ObservacionResponse(
    Long id,
    String estudianteNombreUsuario,
    String unidadTitulo,
    String texto,
    boolean validado,
    String docenteNombreUsuario,
    long fechaActualizacion
) {
    public static ObservacionResponse desde(Observacion observacion) {
        return new ObservacionResponse(
            observacion.getId(),
            observacion.getEstudianteNombreUsuario(),
            observacion.getUnidadTitulo(),
            observacion.getTexto(),
            observacion.isValidado(),
            observacion.getDocenteNombreUsuario(),
            observacion.getFechaActualizacion()
        );
    }
}
