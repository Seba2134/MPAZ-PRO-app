package cl.duoc.mpazpro.resultados.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Valida los JWT que emite ms-usuarios. Este microservicio NO genera tokens.
 *
 * Un JWT tiene 3 partes: encabezado.contenido.firma. ms-usuarios lo firma (HS256) con la clave
 * mpazpro.jwt.secreto; aquí se comprueba esa firma con la MISMA clave. Si coincide y no está vencido,
 * los datos del contenido ("sub" = nombre de usuario y "rol") son confiables sin preguntarle a nadie.
 * Por eso ms-resultados sigue funcionando aunque ms-usuarios esté apagado.
 */
@Service
public class JwtService {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;

    public JwtService(@Value("${mpazpro.jwt.secreto}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
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

    /** Verifica la firma y el vencimiento; lanza JwtException si algo no calza. */
    private Claims leer(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
