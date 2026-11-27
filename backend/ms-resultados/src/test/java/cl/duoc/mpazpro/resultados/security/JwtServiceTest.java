package cl.duoc.mpazpro.resultados.security;

import cl.duoc.mpazpro.resultados.TokensDePrueba;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(TokensDePrueba.SECRETO);

    @Test
    void unTokenDeMsUsuariosEsValidoYTraeUsuarioYRol() {
        String token = TokensDePrueba.token("ana", "DOCENTE");

        assertTrue(jwtService.validar(token));
        assertEquals("ana", jwtService.nombreUsuario(token));
        assertEquals("DOCENTE", jwtService.rol(token));
    }

    @Test
    void unTokenAlteradoVencidoOConOtraClaveNoEsValido() {
        assertFalse(jwtService.validar(TokensDePrueba.token("ana", "DOCENTE") + "x"));
        assertFalse(jwtService.validar(TokensDePrueba.vencido("ana", "DOCENTE")));
        assertFalse(jwtService.validar(TokensDePrueba.conOtraClave("ana", "DOCENTE")));
        assertFalse(jwtService.validar("esto-no-es-un-token"));
    }
}
