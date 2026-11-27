package cl.duoc.mpazpro.resultados.config;

import cl.duoc.mpazpro.resultados.security.JwtFiltro;
import cl.duoc.mpazpro.resultados.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Reglas de seguridad de ms-resultados:
 * - Sin sesiones (STATELESS): cada petición trae el JWT que emitió ms-usuarios.
 * - CSRF desactivado: la API no usa cookies, así que ese ataque no aplica.
 * - JwtFiltro corre antes del filtro de usuario/contraseña de Spring.
 */
@Configuration
public class SeguridadConfig {

    private static final String ESTUDIANTE = "ESTUDIANTE";
    private static final String DOCENTE = "DOCENTE";
    private static final String JEFATURA_UTP = "JEFATURA_UTP";

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(
        HttpSecurity http,
        JwtService jwtService,
        @Value("${spring.h2.console.enabled:false}") boolean consolaH2
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(reglas -> {
                reglas.requestMatchers(HttpMethod.GET, "/api/salud").permitAll();
                reglas.requestMatchers("/error").permitAll();
                if (consolaH2) {
                    reglas.requestMatchers("/h2-console/**").permitAll();
                }
                reglas.requestMatchers(HttpMethod.POST, "/api/intentos").hasAnyRole(ESTUDIANTE, DOCENTE, JEFATURA_UTP);
                reglas.requestMatchers(HttpMethod.GET, "/api/intentos", "/api/intentos/**").hasAnyRole(DOCENTE, JEFATURA_UTP);
                // El estudiante nunca ve informes (regla del caso)
                reglas.requestMatchers("/api/informes/**").hasAnyRole(DOCENTE, JEFATURA_UTP);
                reglas.requestMatchers("/api/observaciones", "/api/observaciones/**").hasAnyRole(DOCENTE, JEFATURA_UTP);
                reglas.anyRequest().authenticated();
            })
            .exceptionHandling(errores -> errores
                .authenticationEntryPoint((request, response, e) ->
                    escribirError(response, HttpServletResponse.SC_UNAUTHORIZED, "Necesitas iniciar sesión (token inválido o vencido)"))
                .accessDeniedHandler((request, response, e) ->
                    escribirError(response, HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para esto"))
            )
            .addFilterBefore(new JwtFiltro(jwtService), UsernamePasswordAuthenticationFilter.class);

        if (consolaH2) {
            // La consola H2 se dibuja dentro de un frame: solo se permite en desarrollo
            http.headers(encabezados -> encabezados.frameOptions(frame -> frame.sameOrigin()));
        }
        return http.build();
    }

    /**
     * Este microservicio no tiene usuarios propios (los datos vienen en el token).
     * Se declara vacío para que Spring no cree su usuario de prueba con clave aleatoria.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    private static void escribirError(HttpServletResponse response, int estado, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
