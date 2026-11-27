package cl.duoc.mpazpro.usuarios.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba ms-usuarios completo (login real + JWT) con la base en memoria y los usuarios de prueba. */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void saludRespondeSinToken() throws Exception {
        mockMvc.perform(get("/api/salud"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ok"));
    }

    @Test
    void yoConTokenDevuelveSusDatos() throws Exception {
        String token = login("ana", "ana123");
        mockMvc.perform(get("/api/usuarios/yo").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreUsuario").value("ana"))
            .andExpect(jsonPath("$.nombre").value("Profesora Ana"))
            .andExpect(jsonPath("$.rol").value("DOCENTE"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void yoSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/usuarios/yo"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void yoConTokenAlteradoResponde401() throws Exception {
        String token = login("tomas", "1234");
        mockMvc.perform(get("/api/usuarios/yo").header("Authorization", "Bearer " + token + "x"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConClaveMalaResponde401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"ana\",\"password\":\"mala\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void loginConCamposVaciosResponde400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    /** Inicia sesión de verdad contra la API y devuelve el token. */
    private String login(String usuario, String clave) throws Exception {
        String json = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"" + usuario + "\",\"password\":\"" + clave + "\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        assertTrue(m.find(), "La respuesta del login no trae token");
        return m.group(1);
    }
}
