package cl.duoc.mpazpro.api.controller;

import cl.duoc.mpazpro.api.dto.IntentoRequest;
import cl.duoc.mpazpro.api.dto.IntentoResponse;
import cl.duoc.mpazpro.api.model.Rol;
import cl.duoc.mpazpro.api.service.IntentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Los permisos por rol están en SeguridadConfig; la regla «solo sus propios intentos» en IntentoService. */
@RestController
@RequestMapping("/api/intentos")
public class IntentoController {

    private final IntentoService intentoService;

    public IntentoController(IntentoService intentoService) {
        this.intentoService = intentoService;
    }

    /** 201 si se creó · 200 si ya existía (idempotente) · 400 · 401 · 403. */
    @PostMapping
    public ResponseEntity<IntentoResponse> registrar(@Valid @RequestBody IntentoRequest datos, Authentication autenticacion) {
        var resultado = intentoService.registrar(datos, autenticacion.getName(), rolDe(autenticacion));
        HttpStatus estado = resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(resultado.intento());
    }

    /** Ejemplo: GET /api/intentos?estudiante=tomas */
    @GetMapping
    public List<IntentoResponse> listar(@RequestParam("estudiante") String estudiante) {
        return intentoService.listarPorEstudiante(estudiante);
    }

    @GetMapping("/{id}")
    public IntentoResponse buscar(@PathVariable Long id) {
        return intentoService.buscar(id);
    }

    /** El rol viene en la autoridad "ROLE_<rol>" que dejó JwtFiltro. */
    private static Rol rolDe(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a != null && a.startsWith("ROLE_"))
            .map(a -> Rol.valueOf(a.substring("ROLE_".length())))
            .findFirst()
            .orElse(Rol.ESTUDIANTE);  // si no se sabe, se trata con las reglas más estrictas
    }
}
