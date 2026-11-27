package cl.duoc.mpazpro.usuarios.controller;

import cl.duoc.mpazpro.usuarios.dto.LoginRequest;
import cl.duoc.mpazpro.usuarios.dto.LoginResponse;
import cl.duoc.mpazpro.usuarios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 200 con el token · 401 credenciales malas · 400 campos vacíos. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }
}
