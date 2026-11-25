package cl.duoc.mpazpro.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SaludControllerTest {

    @Autowired
    private SaludController saludController;

    @Test
    void contextoYControladorCarganCorrectamente() {
        assertThat(saludController).isNotNull();
    }

    @Test
    void endpointSaludRetornaRespuestaValida() {
        var respuesta = saludController.salud();
        assertThat(respuesta).isNotNull();
    }
}