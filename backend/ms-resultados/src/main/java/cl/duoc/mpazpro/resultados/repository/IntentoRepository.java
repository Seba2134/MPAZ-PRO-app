package cl.duoc.mpazpro.resultados.repository;

import cl.duoc.mpazpro.resultados.model.Intento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntentoRepository extends JpaRepository<Intento, Long> {

    /** Para que registrar sea idempotente: un intento se identifica por estudiante + actividad + número. */
    Optional<Intento> findByEstudianteNombreUsuarioAndActividadIdAndNumeroIntento(
        String estudianteNombreUsuario, Long actividadId, int numeroIntento);

    /** Los intentos de un estudiante, del más reciente al más antiguo. */
    List<Intento> findByEstudianteNombreUsuarioOrderByFechaFinDesc(String estudianteNombreUsuario);
}
