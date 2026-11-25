package cl.duoc.mpazpro.api.service;

import cl.duoc.mpazpro.api.dto.RendimientoOaResponse;
import cl.duoc.mpazpro.api.model.Intento;
import cl.duoc.mpazpro.api.model.Respuesta;
import cl.duoc.mpazpro.api.model.TipoActividad;
import cl.duoc.mpazpro.api.repository.IntentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** El informe por OA, con el mismo criterio que la app: el MEJOR intento de la prueba final. */
@Service
public class InformeService {

    private final IntentoRepository intentoRepository;

    public InformeService(IntentoRepository intentoRepository) {
        this.intentoRepository = intentoRepository;
    }

    /**
     * Rendimiento de un estudiante por OA en una unidad.
     * Si no ha rendido la prueba final de esa unidad, la lista viene vacía.
     */
    @Transactional(readOnly = true)
    public List<RendimientoOaResponse> rendimientoPorOa(String nombreUsuario, String unidadTitulo) {
        Optional<Intento> mejor = intentoRepository
            .findByEstudianteNombreUsuarioOrderByFechaFinDesc(nombreUsuario.trim().toLowerCase(Locale.ROOT))
            .stream()
            .filter(i -> i.getTipo() == TipoActividad.PRUEBA_FINAL)
            .filter(i -> i.getUnidadTitulo().equalsIgnoreCase(unidadTitulo.trim()))
            // Más correctas y, si empatan, el más reciente
            .max(Comparator.comparingInt(Intento::getCorrectas).thenComparingInt(Intento::getNumeroIntento));

        return mejor.map(InformeService::agruparPorOa).orElse(List.of());
    }

    private static List<RendimientoOaResponse> agruparPorOa(Intento intento) {
        Map<String, List<Respuesta>> porOa = new LinkedHashMap<>();
        intento.getRespuestas().stream()
            .sorted(Comparator.comparing(Respuesta::getOaCodigo))
            .forEach(r -> porOa.computeIfAbsent(r.getOaCodigo(), codigo -> new ArrayList<>()).add(r));

        return porOa.entrySet().stream().map(entrada -> {
            List<Respuesta> respuestas = entrada.getValue();
            int correctas = (int) respuestas.stream().filter(Respuesta::isEsCorrecta).count();
            int total = respuestas.size();
            int porcentaje = total == 0 ? 0 : correctas * 100 / total;
            return new RendimientoOaResponse(
                entrada.getKey(), respuestas.get(0).getOaDescripcion(), correctas, total, porcentaje);
        }).toList();
    }
}
