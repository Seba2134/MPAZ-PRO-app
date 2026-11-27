package cl.duoc.mpazpro.usuarios.config;

import cl.duoc.mpazpro.usuarios.repository.UsuarioRepository;
import cl.duoc.mpazpro.usuarios.security.JwtFiltro;
import cl.duoc.mpazpro.usuarios.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Reglas de seguridad de ms-usuarios:
 * - Sin sesiones (STATELESS): cada petición trae su JWT.
 * - CSRF desactivado: la API no usa cookies, así que ese ataque no aplica.
 * - JwtFiltro corre antes del filtro de usuario/contraseña de Spring.
 */
@Configuration
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(
        HttpSecurity http,
        JwtService jwtService,
        UsuarioRepository usuarioRepository,
        @Value("${spring.h2.console.enabled:false}") boolean consolaH2
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(reglas -> {
                reglas.requestMatchers(HttpMethod.GET, "/api/salud").permitAll();
                reglas.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();
                reglas.requestMatchers("/error").permitAll();
                if (consolaH2) {
                    reglas.requestMatchers("/h2-console/**").permitAll();
                }
                // /api/usuarios/yo y todo lo demás: cualquier usuario con un token válido
                reglas.anyRequest().authenticated();
            })
            .exceptionHandling(errores -> errores
                .authenticationEntryPoint((request, response, e) ->
                    escribirError(response, HttpServletResponse.SC_UNAUTHORIZED, "Necesitas iniciar sesión (token inválido o vencido)"))
                .accessDeniedHandler((request, response, e) ->
                    escribirError(response, HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para esto"))
            )
            .addFilterBefore(new JwtFiltro(jwtService, usuarioRepository), UsernamePasswordAuthenticationFilter.class);

        if (consolaH2) {
            // La consola H2 se dibuja dentro de un frame: solo se permite en desarrollo
            http.headers(encabezados -> encabezados.frameOptions(frame -> frame.sameOrigin()));
        }
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cómo encontrar a un usuario por su nombre. La API no lo usa para el login (eso lo hace AuthService),
     * pero al declararlo Spring no crea su usuario de prueba con clave aleatoria.
     */
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return nombreUsuario -> usuarioRepository.findByNombreUsuario(nombreUsuario)
            .map(usuario -> User.withUsername(usuario.getNombreUsuario())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario"));
    }

    private static void escribirError(HttpServletResponse response, int estado, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
