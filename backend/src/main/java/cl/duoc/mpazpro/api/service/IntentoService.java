package cl.duoc.mpazpro.api.service;

import cl.duoc.mpazpro.api.dto.IntentoRequest;
import cl.duoc.mpazpro.api.dto.IntentoResponse;
import cl.duoc.mpazpro.api.exception.ErrorApi;
import cl.duoc.mpazpro.api.model.Intento;
import cl.duoc.mpazpro.api.model.Respuesta;
import cl.duoc.mpazpro.api.model.Rol;
import cl.duoc.mpazpro.api.repository.IntentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/** Guarda y entrega las copias de los intentos que envía la app. */
@Service
public class IntentoService {

    /** El intento guardado, y si se creó ahora (201) o ya existía (200). */
    public record ResultadoRegistro(IntentoResponse intento, boolean creado) {
    }

    private final IntentoRepository intentoRepository;

    public IntentoService(IntentoRepository intentoRepository) {
        this.intentoRepository = intentoRepository;
    }

    /**
     * Registra un intento terminado. Es idempotente: si la app lo envía dos veces
     * (por ejemplo, porque se cortó la conexión), se guarda una sola vez.
     *
     * @param enviadoPor el nombre de usuario del token (quien lo envía)
     * @param rolQueEnvia su rol: un estudiante solo puede enviar sus propios intentos
     */
    @Transactional
    public ResultadoRegistro registrar(IntentoRequest datos, String enviadoPor, Rol rolQueEnvia) {
        String estudiante = normalizar(datos.estudianteNombreUsuario());

        if (rolQueEnvia == Rol.ESTUDIANTE && !estudiante.equals(normalizar(enviadoPor))) {
            throw new ErrorApi.SinPermiso("Un estudiante solo puede enviar sus propios resultados");
        }
        if (datos.correctas() > datos.total()) {
            throw new ErrorApi.DatosInvalidos("Las respuestas correctas no pueden ser más que el total");
        }
        if (datos.fechaFin() < datos.fechaInicio()) {
            throw new ErrorApi.DatosInvalidos("La fecha de término no puede ser anterior a la de inicio");
        }

        var existente = intentoRepository.findByEstudianteNombreUsuarioAndActividadIdAndNumeroIntento(
            estudiante, datos.actividadId(), datos.numeroIntento());
        if (existente.isPresent()) {
            return new ResultadoRegistro(IntentoResponse.desde(existente.get()), false);
        }

        Intento intento = new Intento(
            estudiante,
            datos.estudianteNombre().trim(),
            datos.cursoNombre().trim(),
            datos.asignaturaNombre().trim(),
            datos.unidadTitulo().trim(),
            datos.actividadId(),
            datos.actividadTitulo().trim(),
            datos.tipo(),
            datos.numeroIntento(),
            datos.fechaInicio(),
            datos.fechaFin(),
            datos.correctas(),
            datos.total(),
            normalizar(enviadoPor),
            System.currentTimeMillis()
        );
        datos.respuestas().forEach(r -> intento.agregarRespuesta(
            new Respuesta(r.preguntaId(), r.enunciado(), r.oaCodigo(), r.oaDescripcion(), r.esCorrecta())
        ));
        return new ResultadoRegistro(IntentoResponse.desde(intentoRepository.save(intento)), true);
    }

    /** Los intentos de un estudiante, del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<IntentoResponse> listarPorEstudiante(String nombreUsuario) {
        return intentoRepository.findByEstudianteNombreUsuarioOrderByFechaFinDesc(normalizar(nombreUsuario))
            .stream()
            .map(IntentoResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    public IntentoResponse buscar(Long id) {
        return intentoRepository.findById(id)
            .map(IntentoResponse::desde)
            .orElseThrow(() -> new ErrorApi.NoEncontrado("No existe el intento " + id));
    }

    private static String normalizar(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }
}
