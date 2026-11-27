package cl.duoc.mpazpro.usuarios.service;

import cl.duoc.mpazpro.usuarios.dto.LoginRequest;
import cl.duoc.mpazpro.usuarios.dto.LoginResponse;
import cl.duoc.mpazpro.usuarios.exception.ErrorApi;
import cl.duoc.mpazpro.usuarios.model.Rol;
import cl.duoc.mpazpro.usuarios.model.Usuario;
import cl.duoc.mpazpro.usuarios.repository.UsuarioRepository;
import cl.duoc.mpazpro.usuarios.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService("una-clave-de-pruebas-de-al-menos-32-caracteres", 8);
    private final AuthService authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);

    private Usuario ana;

    @BeforeEach
    void preparar() {
        ana = new Usuario("ana", "Profesora Ana", passwordEncoder.encode("ana123"), Rol.DOCENTE);
        when(usuarioRepository.findByNombreUsuario("ana")).thenReturn(Optional.of(ana));
        when(usuarioRepository.findByNombreUsuario("nadie")).thenReturn(Optional.empty());
    }

    @Test
    void claveCorrectaEntregaUnToken() {
        LoginResponse respuesta = authService.login(new LoginRequest("  Ana ", "ana123"));

        assertTrue(jwtService.validar(respuesta.token()));
        assertEquals("ana", jwtService.nombreUsuario(respuesta.token()));
        assertEquals("DOCENTE", respuesta.rol());
        assertEquals("Profesora Ana", respuesta.nombre());
    }

    @Test
    void claveMalaDaError() {
        var error = assertThrows(ErrorApi.CredencialesInvalidas.class,
            () -> authService.login(new LoginRequest("ana", "otra")));
        assertEquals(AuthService.CREDENCIALES_INVALIDAS, error.getMessage());
    }

    @Test
    void usuarioInexistenteDaElMismoError() {
        var error = assertThrows(ErrorApi.CredencialesInvalidas.class,
            () -> authService.login(new LoginRequest("nadie", "1234")));
        assertEquals(AuthService.CREDENCIALES_INVALIDAS, error.getMessage());
    }

    @Test
    void usuarioInactivoDaError() {
        ana.setActivo(false);
        var error = assertThrows(ErrorApi.CredencialesInvalidas.class,
            () -> authService.login(new LoginRequest("ana", "ana123")));
        assertEquals(AuthService.CUENTA_INACTIVA, error.getMessage());
    }
}
