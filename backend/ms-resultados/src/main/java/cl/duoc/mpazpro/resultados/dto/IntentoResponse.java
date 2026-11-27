package cl.duoc.mpazpro.resultados.dto;

import cl.duoc.mpazpro.resultados.model.Intento;
import cl.duoc.mpazpro.resultados.model.TipoActividad;

import java.util.List;

/** Un intento guardado, tal como sale por la API (la entidad JPA nunca sale directamente). */
public record IntentoResponse(
    Long id,
    String estudianteNombreUsuario,
    String estudianteNombre,
    String cursoNombre,
    String asignaturaNombre,
    String unidadTitulo,
    Long actividadId,
    String actividadTitulo,
    TipoActividad tipo,
    int numeroIntento,
    long fechaInicio,
    long fechaFin,
    int correctas,
    int total,
    String enviadoPor,
    long fechaRecepcion,
    List<RespuestaResponse> respuestas
) {
    public static IntentoResponse desde(Intento intento) {
        return new IntentoResponse(
            intento.getId(),
            intento.getEstudianteNombreUsuario(),
            intento.getEstudianteNombre(),
            intento.getCursoNombre(),
            intento.getAsignaturaNombre(),
            intento.getUnidadTitulo(),
            intento.getActividadId(),
            intento.getActividadTitulo(),
            intento.getTipo(),
            intento.getNumeroIntento(),
            intento.getFechaInicio(),
            intento.getFechaFin(),
            intento.getCorrectas(),
            intento.getTotal(),
            intento.getEnviadoPor(),
            intento.getFechaRecepcion(),
            intento.getRespuestas().stream().map(RespuestaResponse::desde).toList()
        );
    }
}
