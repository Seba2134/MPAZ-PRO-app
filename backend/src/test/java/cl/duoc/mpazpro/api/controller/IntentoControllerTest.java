package cl.duoc.mpazpro.api.controller;

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

/**
 * Prueba la API completa (seguridad + JWT + controladores) con la base en memoria
 * y los usuarios de prueba que crea DatosIniciales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class IntentoControllerTest {

    private static final String INTENTO_VALIDO = """
        {
          "estudianteNombreUsuario": "tomas", "estudianteNombre": "Tomás", "cursoNombre": "2.º A",
          "asignaturaNombre": "Lenguaje y Comunicación", "unidadTitulo": "Leemos cuentos",
          "actividadId": %d, "actividadTitulo": "Prueba final: Leemos cuentos", "tipo": "PRUEBA_FINAL",
          "numeroIntento": 1, "fechaInicio": 1764000000000, "fechaFin": 1764000300000,
          "correctas": 1, "total": 2,
          "respuestas": [
            { "preguntaId": 3, "enunciado": "¿Qué es un personaje?", "oaCodigo": "OA 1",
              "oaDescripcion": "Personajes", "esCorrecta": true },
            { "preguntaId": 4, "enunciado": "¿Cuál es el lugar?", "oaCodigo": "OA 1",
              "oaDescripcion": "Personajes", "esCorrecta": false }
          ]
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void saludRespondeSinToken() throws Exception {
        mockMvc.perform(get("/api/salud"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ok"));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/intentos").param("estudiante", "tomas"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAlteradoResponde401() throws Exception {
        String token = login("ana", "ana123");
        mockMvc.perform(get("/api/intentos").param("estudiante", "tomas")
                .header("Authorization", "Bearer " + token + "x"))
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

    @Test
    void estudiantePidiendoUnInformeResponde403() throws Exception {
        String token = login("tomas", "1234");
        mockMvc.perform(get("/api/informes/estudiante/tomas").param("unidadTitulo", "Leemos cuentos")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void docentePidiendoUnInformeResponde200() throws Exception {
        String token = login("ana", "ana123");
        mockMvc.perform(get("/api/informes/estudiante/tomas").param("unidadTitulo", "Leemos cuentos")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void postValidoResponde201YRepetirloResponde200() throws Exception {
        String token = login("tomas", "1234");
        String cuerpo = INTENTO_VALIDO.formatted(100);

        mockMvc.perform(post("/api/intentos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estudianteNombreUsuario").value("tomas"));

        mockMvc.perform(post("/api/intentos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isOk());
    }

    @Test
    void estudianteEnviandoPorOtroResponde403() throws Exception {
        String token = login("sofia", "1234");
        mockMvc.perform(post("/api/intentos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(INTENTO_VALIDO.formatted(200)))
            .andExpect(status().isForbidden());
    }

    @Test
    void cuerpoInvalidoResponde400() throws Exception {
        String token = login("tomas", "1234");
        mockMvc.perform(post("/api/intentos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"estudianteNombreUsuario\":\"tomas\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos").exists());
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
