package cl.duoc.mpazpro.usuarios.controller;

import cl.duoc.mpazpro.usuarios.dto.UsuarioResponse;
import cl.duoc.mpazpro.usuarios.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Quién es el dueño del token: sirve para comprobar que un token funciona. 200 · 401 sin token. */
    @GetMapping("/yo")
    public UsuarioResponse yo(Authentication autenticacion) {
        return usuarioService.yo(autenticacion.getName());
    }
}
