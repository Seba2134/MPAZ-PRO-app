package cl.duoc.mpazpro.api.service;

import cl.duoc.mpazpro.api.dto.RendimientoOaResponse;
import cl.duoc.mpazpro.api.model.Intento;
import cl.duoc.mpazpro.api.model.Respuesta;
import cl.duoc.mpazpro.api.model.TipoActividad;
import cl.duoc.mpazpro.api.repository.IntentoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InformeServiceTest {

    private final IntentoRepository intentoRepository = mock(IntentoRepository.class);
    private final InformeService informeService = new InformeService(intentoRepository);

    /** Una prueba final de 4 preguntas (2 por OA); [aciertos] dice cuáles fueron correctas. */
    private static Intento prueba(int numero, boolean... aciertos) {
        int correctas = 0;
        for (boolean acierto : aciertos) if (acierto) correctas++;
        Intento intento = new Intento("tomas", "Tomás", "2.º A", "Lenguaje", "Leemos cuentos",
            2L, "Prueba final", TipoActividad.PRUEBA_FINAL, numero, 1L, 2L + numero, correctas, 4, "tomas", 3L);
        String[] oas = {"OA 2", "OA 1", "OA 2", "OA 1"};
        for (int i = 0; i < aciertos.length; i++) {
            intento.agregarRespuesta(new Respuesta((long) i, "Pregunta " + i, oas[i], "Descripción " + oas[i], aciertos[i]));
        }
        return intento;
    }

    @Test
    void conDosIntentosUsaElDeMasCorrectasYAgrupaPorOa() {
        Intento peor = prueba(1, false, true, false, false);  // 1 de 4
        Intento mejor = prueba(2, true, true, false, true);   // 3 de 4
        when(intentoRepository.findByEstudianteNombreUsuarioOrderByFechaFinDesc("tomas"))
            .thenReturn(List.of(mejor, peor));

        List<RendimientoOaResponse> informe = informeService.rendimientoPorOa("tomas", "Leemos cuentos");

        assertEquals(2, informe.size());
        assertEquals("OA 1", informe.get(0).oaCodigo());   // ordenado por código
        assertEquals(2, informe.get(0).correctas());
        assertEquals(2, informe.get(0).total());
        assertEquals(100, informe.get(0).porcentaje());
        assertEquals("OA 2", informe.get(1).oaCodigo());
        assertEquals(1, informe.get(1).correctas());
        assertEquals(50, informe.get(1).porcentaje());
    }

    @Test
    void ignoraLosDesafiosYOtrasUnidades() {
        Intento desafio = new Intento("tomas", "Tomás", "2.º A", "Lenguaje", "Leemos cuentos",
            1L, "Desafío", TipoActividad.DESAFIO, 1, 1L, 2L, 2, 2, "tomas", 3L);
        Intento otraUnidad = new Intento("tomas", "Tomás", "2.º A", "Matemática", "Números",
            9L, "Prueba final", TipoActividad.PRUEBA_FINAL, 1, 1L, 2L, 4, 4, "tomas", 3L);
        when(intentoRepository.findByEstudianteNombreUsuarioOrderByFechaFinDesc("tomas"))
            .thenReturn(List.of(desafio, otraUnidad));

        assertTrue(informeService.rendimientoPorOa("tomas", "Leemos cuentos").isEmpty());
    }

    @Test
    void sinIntentosDevuelveListaVacia() {
        when(intentoRepository.findByEstudianteNombreUsuarioOrderByFechaFinDesc("sofia")).thenReturn(List.of());

        assertTrue(informeService.rendimientoPorOa("sofia", "Leemos cuentos").isEmpty());
    }
}
