package cl.duoc.mpazpro.resultados.repository;

import cl.duoc.mpazpro.resultados.model.Observacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ObservacionRepository extends JpaRepository<Observacion, Long> {

    /** La observación de un estudiante en una unidad (hay a lo más una). */
    Optional<Observacion> findByEstudianteNombreUsuarioAndUnidadTitulo(String estudianteNombreUsuario, String unidadTitulo);
}
