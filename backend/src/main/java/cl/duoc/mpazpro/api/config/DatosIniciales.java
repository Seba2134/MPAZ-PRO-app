package cl.duoc.mpazpro.api.config;

import cl.duoc.mpazpro.api.model.Rol;
import cl.duoc.mpazpro.api.model.Usuario;
import cl.duoc.mpazpro.api.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, si no hay usuarios, crea los mismos 8 usuarios de prueba que la app.
 * Las claves se guardan con BCrypt (nunca en texto plano). Todos los datos son ficticios.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        crear("utp", "Jefatura UTP", "utp123", Rol.JEFATURA_UTP);
        crear("ana", "Profesora Ana", "ana123", Rol.DOCENTE);
        crear("carlos", "Profesor Carlos", "carlos123", Rol.DOCENTE);
        crear("daniela", "Profesora Daniela", "daniela123", Rol.DOCENTE);
        crear("tomas", "Tomás", "1234", Rol.ESTUDIANTE);
        crear("sofia", "Sofía", "1234", Rol.ESTUDIANTE);
        crear("benjamin", "Benjamín", "1234", Rol.ESTUDIANTE);
        crear("isidora", "Isidora", "1234", Rol.ESTUDIANTE);
    }

    private void crear(String nombreUsuario, String nombre, String clave, Rol rol) {
        usuarioRepository.save(new Usuario(nombreUsuario, nombre, passwordEncoder.encode(clave), rol));
    }
}
