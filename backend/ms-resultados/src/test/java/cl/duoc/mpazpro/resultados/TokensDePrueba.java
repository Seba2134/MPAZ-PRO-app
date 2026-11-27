package cl.duoc.mpazpro.resultados;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera tokens para las pruebas, firmados igual que los de ms-usuarios.
 * ms-resultados no tiene login: en producción los tokens vienen de ms-usuarios.
 */
public final class TokensDePrueba {

    /** La misma clave de src/test/resources/application.properties. */
    public static final String SECRETO = "mpazpro-clave-de-pruebas-2026-solo-para-tests";

    private TokensDePrueba() {
    }

    /** Un token válido por 1 hora para ese usuario y rol. */
    public static String token(String nombreUsuario, String rol) {
        return firmar(nombreUsuario, rol, SECRETO, 60 * 60 * 1000L);
    }

    /** Un token ya vencido. */
    public static String vencido(String nombreUsuario, String rol) {
        return firmar(nombreUsuario, rol, SECRETO, -60 * 1000L);
    }

    /** Un token firmado con otra clave (como si lo hubiera hecho alguien que no es ms-usuarios). */
    public static String conOtraClave(String nombreUsuario, String rol) {
        return firmar(nombreUsuario, rol, "otra-clave-distinta-de-al-menos-32-caracteres", 60 * 60 * 1000L);
    }

    private static String firmar(String nombreUsuario, String rol, String secreto, long duracionMs) {
        SecretKey clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
            .subject(nombreUsuario)
            .claim("rol", rol)
            .issuedAt(new Date(ahora))
            .expiration(new Date(ahora + duracionMs))
            .signWith(clave, Jwts.SIG.HS256)
            .compact();
    }
}
