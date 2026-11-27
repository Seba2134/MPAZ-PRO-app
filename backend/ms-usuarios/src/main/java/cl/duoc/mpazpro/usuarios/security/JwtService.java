package cl.duoc.mpazpro.usuarios.security;

import cl.duoc.mpazpro.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * Crea y valida los JWT (JSON Web Token).
 *
 * Un JWT es un texto firmado con 3 partes separadas por puntos: encabezado.contenido.firma.
 * El contenido lleva "sub" (el nombre de usuario), "rol" y "exp" (cuándo vence).
 * La firma se calcula con la clave secreta del servidor (HS256): si alguien cambia
 * una sola letra del contenido, la firma deja de coincidir y el token se rechaza.
 * El servidor no guarda sesiones (stateless): todo lo que necesita va en el token.
 */
@Service
public class JwtService {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(
        @Value("${mpazpro.jwt.secreto}") String secreto,
        @Value("${mpazpro.jwt.horas}") long horas
    ) {
        // HS256 exige una clave de al menos 256 bits (32 caracteres)
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracion = Duration.ofHours(horas);
    }

    /** Genera el token de un usuario: vence en las horas configuradas. */
    public String generarToken(Usuario usuario) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
            .subject(usuario.getNombreUsuario())
            .claim(CLAIM_ROL, usuario.getRol().name())
            .issuedAt(new Date(ahora))
            .expiration(new Date(ahora + duracion.toMillis()))
            .signWith(clave, Jwts.SIG.HS256)  // se fija HS256; si no, JJWT elige según el largo de la clave
            .compact();
    }

    /** true si la firma es correcta y el token no ha vencido. */
    public boolean validar(String token) {
        try {
            leer(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String nombreUsuario(String token) {
        return leer(token).getSubject();
    }

    public String rol(String token) {
        return leer(token).get(CLAIM_ROL, String.class);
    }

    /** Cuándo vence el token, en milisegundos. */
    public long expiraEn(String token) {
        return leer(token).getExpiration().getTime();
    }

    /** Verifica la firma y el vencimiento; lanza JwtException si algo no calza. */
    private Claims leer(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
