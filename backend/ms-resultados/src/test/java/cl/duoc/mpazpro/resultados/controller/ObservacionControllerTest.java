package cl.duoc.mpazpro.resultados.controller;

import cl.duoc.mpazpro.resultados.TokensDePrueba;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Recibir (GET), enviar (POST) y actualizar (PUT) observaciones, con seguridad real y base en memoria. */
@SpringBootTest
@AutoConfigureMockMvc
class ObservacionControllerTest {

    private static final String ANA = "Bearer " + TokensDePrueba.token("ana", "DOCENTE");
    private static final String TOMAS = "Bearer " + TokensDePrueba.token("tomas", "ESTUDIANTE");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/observaciones").param("estudiante", "tomas").param("unidadTitulo", "U"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void estudianteResponde403() throws Exception {
        mockMvc.perform(get("/api/observaciones").param("estudiante", "tomas").param("unidadTitulo", "U")
                .header("Authorization", TOMAS))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/observaciones").header("Authorization", TOMAS)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estudianteNombreUsuario\":\"tomas\",\"unidadTitulo\":\"U\",\"texto\":\"x\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getDeUnaQueNoExisteResponde404() throws Exception {
        mockMvc.perform(get("/api/observaciones").param("estudiante", "nadie").param("unidadTitulo", "Sin unidad")
                .header("Authorization", ANA))
            .andExpect(status().isNotFound());
    }

    @Test
    void postCrea201RepetirloDa409YPutActualiza200() throws Exception {
        String cuerpo = "{\"estudianteNombreUsuario\":\"tomas\",\"unidadTitulo\":\"Leemos cuentos\",\"texto\":\"Reconoce bien\"}";

        // Enviar
        String json = mockMvc.perform(post("/api/observaciones").header("Authorization", ANA)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.validado").value(false))
            .andExpect(jsonPath("$.docenteNombreUsuario").value("ana"))
            .andReturn().getResponse().getContentAsString();
        String id = idDe(json);

        // Duplicado → 409 con el id existente
        mockMvc.perform(post("/api/observaciones").header("Authorization", ANA)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.id").value(Long.parseLong(id)));

        // Actualizar: validar con el mismo texto
        mockMvc.perform(put("/api/observaciones/" + id).header("Authorization", ANA)
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Reconoce bien\",\"validado\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.validado").value(true));

        // Recibir
        mockMvc.perform(get("/api/observaciones").param("estudiante", "tomas").param("unidadTitulo", "Leemos cuentos")
                .header("Authorization", ANA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.texto").value("Reconoce bien"))
            .andExpect(jsonPath("$.validado").value(true));
    }

    @Test
    void putDeUnaQueNoExisteResponde404YTextoVacio400() throws Exception {
        mockMvc.perform(put("/api/observaciones/9999").header("Authorization", ANA)
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"x\",\"validado\":false}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/observaciones").header("Authorization", ANA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estudianteNombreUsuario\":\"tomas\",\"unidadTitulo\":\"U\",\"texto\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    private static String idDe(String json) {
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(json);
        assertTrue(m.find(), "La respuesta no trae id");
        return m.group(1);
    }
}
