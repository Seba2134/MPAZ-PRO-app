package cl.duoc.mpazpro.api.security;

import cl.duoc.mpazpro.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Se ejecuta en cada petición: lee el encabezado "Authorization: Bearer <token>".
 * Si el token es válido y el usuario sigue activo, deja al usuario autenticado con la autoridad ROLE_<rol>.
 * Si no hay token, o es inválido o vencido, la petición sigue SIN autenticar
 * y Spring Security responde 401 cuando la ruta lo exige.
 *
 * No es un @Component a propósito: lo crea SeguridadConfig, así no se registra dos veces.
 */
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtFiltro(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
        throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            String token = encabezado.substring(PREFIJO.length()).trim();
            if (jwtService.validar(token)) {
                usuarioRepository.findByNombreUsuario(jwtService.nombreUsuario(token))
                    .filter(usuario -> usuario.isActivo())
                    .ifPresent(usuario -> {
                        var autoridad = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
                        var autenticacion = new UsernamePasswordAuthenticationToken(
                            usuario.getNombreUsuario(), null, List.of(autoridad));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    });
            }
        }
        cadena.doFilter(request, response);
    }
}
