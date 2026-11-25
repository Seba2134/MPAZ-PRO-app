package cl.duoc.mpazpro.api.security;

import cl.duoc.mpazpro.api.model.Rol;
import cl.duoc.mpazpro.api.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRETO = "una-clave-de-pruebas-de-al-menos-32-caracteres";

    private final JwtService jwtService = new JwtService(SECRETO, 8);
    private final Usuario ana = new Usuario("ana", "Profesora Ana", "hash", Rol.DOCENTE);

    @Test
    void unTokenGeneradoEsValidoYTraeUsuarioYRol() {
        String token = jwtService.generarToken(ana);

        assertTrue(jwtService.validar(token));
        assertEquals("ana", jwtService.nombreUsuario(token));
        assertEquals("DOCENTE", jwtService.rol(token));
        assertTrue(jwtService.expiraEn(token) > System.currentTimeMillis());
    }

    @Test
    void unTokenAlteradoNoEsValido() {
        String token = jwtService.generarToken(ana);
        // Se cambia una letra del contenido (la parte del medio): la firma deja de coincidir
        String[] partes = token.split("\\.");
        char letra = partes[1].charAt(5);
        partes[1] = partes[1].substring(0, 5) + (letra == 'A' ? 'B' : 'A') + partes[1].substring(6);
        String alterado = String.join(".", partes);

        assertFalse(jwtService.validar(alterado));
    }

    @Test
    void usaHs256AunqueLaClaveSeaLarga() {
        // La clave de application.properties tiene 54 caracteres: sin fijar el algoritmo, JJWT usaría HS384
        JwtService conClaveLarga = new JwtService("mpazpro-clave-de-desarrollo-2026-no-usar-en-produccion", 8);
        String token = conClaveLarga.generarToken(ana);

        String encabezado = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[0]));
        assertTrue(encabezado.contains("HS256"));
        assertFalse(conClaveLarga.validar(token + "x"));
    }

    @Test
    void unTokenFirmadoConOtraClaveNoEsValido() {
        JwtService otroServidor = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres", 8);
        assertFalse(jwtService.validar(otroServidor.generarToken(ana)));
    }

    @Test
    void unTokenVencidoNoEsValido() {
        // Con duración negativa el token nace vencido
        JwtService vencido = new JwtService(SECRETO, -1);
        assertFalse(jwtService.validar(vencido.generarToken(ana)));
    }

    @Test
    void textoQueNoEsTokenNoEsValido() {
        assertFalse(jwtService.validar("esto-no-es-un-token"));
        assertFalse(jwtService.validar(""));
    }
}
