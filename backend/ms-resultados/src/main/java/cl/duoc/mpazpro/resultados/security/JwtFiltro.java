package cl.duoc.mpazpro.resultados.security;

import cl.duoc.mpazpro.resultados.model.Rol;
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
 * Se ejecuta en cada petición: lee «Authorization: Bearer <token>».
 * Este microservicio no tiene tabla de usuarios: confía en los datos FIRMADOS del token
 * (sub = nombre de usuario, rol) y crea la autoridad ROLE_<rol>. No llama a ms-usuarios.
 * Si no hay token, o es inválido, vencido o trae un rol desconocido, la petición sigue
 * SIN autenticar y Spring Security responde 401 cuando la ruta lo exige.
 *
 * No es un @Component a propósito: lo crea SeguridadConfig, así no se registra dos veces.
 */
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtFiltro(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
        throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            String token = encabezado.substring(PREFIJO.length()).trim();
            if (jwtService.validar(token)) {
                String nombreUsuario = jwtService.nombreUsuario(token);
                Rol rol = rolDesde(jwtService.rol(token));
                if (nombreUsuario != null && !nombreUsuario.isBlank() && rol != null) {
                    var autoridad = new SimpleGrantedAuthority("ROLE_" + rol.name());
                    var autenticacion = new UsernamePasswordAuthenticationToken(nombreUsuario, null, List.of(autoridad));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        }
        cadena.doFilter(request, response);
    }

    /** El rol del token, o null si no es uno de los conocidos. */
    private static Rol rolDesde(String texto) {
        if (texto == null) return null;
        try {
            return Rol.valueOf(texto);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
