package cl.duoc.mpazpro.resultados.controller;

import cl.duoc.mpazpro.resultados.TokensDePrueba;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba ms-resultados completo (seguridad + JWT + controladores) con la base en memoria.
 * Los tokens se firman con TokensDePrueba, igual que los emite ms-usuarios.
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

    private static final String ANA = "Bearer " + TokensDePrueba.token("ana", "DOCENTE");
    private static final String TOMAS = "Bearer " + TokensDePrueba.token("tomas", "ESTUDIANTE");
    private static final String SOFIA = "Bearer " + TokensDePrueba.token("sofia", "ESTUDIANTE");

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
    void tokenAlteradoVencidoOConOtraClaveResponde401() throws Exception {
        for (String token : new String[] {
            TokensDePrueba.token("ana", "DOCENTE") + "x",
            TokensDePrueba.vencido("ana", "DOCENTE"),
            TokensDePrueba.conOtraClave("ana", "DOCENTE"),
            TokensDePrueba.token("ana", "DIRECTOR")   // un rol que no existe
        }) {
            mockMvc.perform(get("/api/intentos").param("estudiante", "tomas")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void docenteConTokenValidoListaIntentos() throws Exception {
        mockMvc.perform(get("/api/intentos").param("estudiante", "tomas").header("Authorization", ANA))
            .andExpect(status().isOk());
    }

    @Test
    void estudiantePidiendoUnInformeResponde403() throws Exception {
        mockMvc.perform(get("/api/informes/estudiante/tomas").param("unidadTitulo", "Leemos cuentos")
                .header("Authorization", TOMAS))
            .andExpect(status().isForbidden());
    }

    @Test
    void docentePidiendoUnInformeResponde200() throws Exception {
        mockMvc.perform(get("/api/informes/estudiante/tomas").param("unidadTitulo", "Leemos cuentos")
                .header("Authorization", ANA))
            .andExpect(status().isOk());
    }

    @Test
    void postValidoResponde201YRepetirloResponde200() throws Exception {
        String cuerpo = INTENTO_VALIDO.formatted(100);

        mockMvc.perform(post("/api/intentos").header("Authorization", TOMAS)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estudianteNombreUsuario").value("tomas"));

        mockMvc.perform(post("/api/intentos").header("Authorization", TOMAS)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isOk());
    }

    @Test
    void estudianteEnviandoPorOtroResponde403() throws Exception {
        mockMvc.perform(post("/api/intentos").header("Authorization", SOFIA)
                .contentType(MediaType.APPLICATION_JSON).content(INTENTO_VALIDO.formatted(200)))
            .andExpect(status().isForbidden());
    }

    @Test
    void cuerpoInvalidoResponde400() throws Exception {
        mockMvc.perform(post("/api/intentos").header("Authorization", TOMAS)
                .contentType(MediaType.APPLICATION_JSON).content("{\"estudianteNombreUsuario\":\"tomas\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos").exists());
    }
}
