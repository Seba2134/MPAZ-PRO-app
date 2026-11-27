package cl.duoc.mpazpro.resultados.service;

import cl.duoc.mpazpro.resultados.dto.ObservacionActualizacion;
import cl.duoc.mpazpro.resultados.dto.ObservacionRequest;
import cl.duoc.mpazpro.resultados.dto.ObservacionResponse;
import cl.duoc.mpazpro.resultados.exception.ErrorApi;
import cl.duoc.mpazpro.resultados.model.Observacion;
import cl.duoc.mpazpro.resultados.repository.ObservacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Las observaciones del docente: recibir (GET), enviar (POST) y actualizar (PUT).
 * Los permisos por rol están en SeguridadConfig (solo DOCENTE y JEFATURA_UTP).
 */
@Service
public class ObservacionService {

    private final ObservacionRepository observacionRepository;

    public ObservacionService(ObservacionRepository observacionRepository) {
        this.observacionRepository = observacionRepository;
    }

    @Transactional(readOnly = true)
    public ObservacionResponse buscar(String estudiante, String unidadTitulo) {
        return observacionRepository
            .findByEstudianteNombreUsuarioAndUnidadTitulo(normalizar(estudiante), unidadTitulo.trim())
            .map(ObservacionResponse::desde)
            .orElseThrow(() -> new ErrorApi.NoEncontrado("No hay observación para ese estudiante en esa unidad"));
    }

    /** Crea la observación. Si ya existe una para ese estudiante y unidad → 409 con su id. */
    @Transactional
    public ObservacionResponse crear(ObservacionRequest datos, String docente) {
        String estudiante = normalizar(datos.estudianteNombreUsuario());
        String unidad = datos.unidadTitulo().trim();
        observacionRepository.findByEstudianteNombreUsuarioAndUnidadTitulo(estudiante, unidad)
            .ifPresent(existente -> {
                throw new ErrorApi.Conflicto("Ya existe una observación para ese estudiante en esa unidad", existente.getId());
            });

        Observacion nueva = new Observacion(estudiante, unidad, datos.texto().trim(), normalizar(docente), System.currentTimeMillis());
        return ObservacionResponse.desde(observacionRepository.save(nueva));
    }

    /** Cambia el texto y/o la validación. Si el texto cambia, la validación se pierde. */
    @Transactional
    public ObservacionResponse actualizar(Long id, ObservacionActualizacion datos, String docente) {
        Observacion observacion = observacionRepository.findById(id)
            .orElseThrow(() -> new ErrorApi.NoEncontrado("No existe la observación " + id));
        observacion.actualizar(datos.texto().trim(), datos.validado(), normalizar(docente), System.currentTimeMillis());
        return ObservacionResponse.desde(observacionRepository.save(observacion));
    }

    private static String normalizar(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }
}
