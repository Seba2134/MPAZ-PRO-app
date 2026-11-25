package cl.duoc.mpazpro.api.service;

import cl.duoc.mpazpro.api.dto.LoginRequest;
import cl.duoc.mpazpro.api.dto.LoginResponse;
import cl.duoc.mpazpro.api.exception.ErrorApi;
import cl.duoc.mpazpro.api.model.Usuario;
import cl.duoc.mpazpro.api.repository.UsuarioRepository;
import cl.duoc.mpazpro.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Inicio de sesión: revisa la clave con BCrypt y entrega un JWT. */
@Service
public class AuthService {

    static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";
    static final String CUENTA_INACTIVA = "Tu cuenta está desactivada. Habla con Jefatura UTP.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Si el usuario no existe o la clave es incorrecta, el error es el MISMO (igual que en la app),
     * para no revelar qué nombres de usuario existen.
     */
    public LoginResponse login(LoginRequest datos) {
        String nombreUsuario = datos.nombreUsuario().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
            .filter(u -> passwordEncoder.matches(datos.password(), u.getPasswordHash()))
            .orElseThrow(() -> new ErrorApi.CredencialesInvalidas(CREDENCIALES_INVALIDAS));

        if (!usuario.isActivo()) {
            throw new ErrorApi.CredencialesInvalidas(CUENTA_INACTIVA);
        }

        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token, usuario.getRol().name(), usuario.getNombre(), jwtService.expiraEn(token));
    }
}
