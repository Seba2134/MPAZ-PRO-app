package cl.duoc.mpazpro.api.service;

import cl.duoc.mpazpro.api.dto.IntentoRequest;
import cl.duoc.mpazpro.api.dto.RespuestaRequest;
import cl.duoc.mpazpro.api.exception.ErrorApi;
import cl.duoc.mpazpro.api.model.Intento;
import cl.duoc.mpazpro.api.model.Rol;
import cl.duoc.mpazpro.api.model.TipoActividad;
import cl.duoc.mpazpro.api.repository.IntentoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntentoServiceTest {

    private final IntentoRepository intentoRepository = mock(IntentoRepository.class);
    private final IntentoService intentoService = new IntentoService(intentoRepository);

    /** El intento del ejemplo de la especificación. */
    static IntentoRequest intentoDeTomas(int correctas, int total) {
        return new IntentoRequest(
            "tomas", "Tomás", "2.º A", "Lenguaje y Comunicación", "Leemos cuentos",
            2L, "Prueba final: Leemos cuentos", TipoActividad.PRUEBA_FINAL,
            1, 1764000000000L, 1764000300000L, correctas, total,
            List.of(new RespuestaRequest(3L, "¿Qué es un personaje?", "OA 1", "Personajes", true))
        );
    }

    @Test
    void registrarUnoNuevoLoGuarda() {
        when(intentoRepository.findByEstudianteNombreUsuarioAndActividadIdAndNumeroIntento(anyString(), anyLong(), anyInt()))
            .thenReturn(Optional.empty());
        when(intentoRepository.save(any(Intento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = intentoService.registrar(intentoDeTomas(3, 4), "tomas", Rol.ESTUDIANTE);

        assertTrue(resultado.creado());
        assertEquals("tomas", resultado.intento().estudianteNombreUsuario());
        assertEquals(1, resultado.intento().respuestas().size());
        verify(intentoRepository).save(any(Intento.class));
    }

    @Test
    void repetirloNoLoDuplica() {
        Intento existente = new Intento("tomas", "Tomás", "2.º A", "Lenguaje y Comunicación", "Leemos cuentos",
            2L, "Prueba final: Leemos cuentos", TipoActividad.PRUEBA_FINAL, 1, 1L, 2L, 3, 4, "tomas", 3L);
        when(intentoRepository.findByEstudianteNombreUsuarioAndActividadIdAndNumeroIntento("tomas", 2L, 1))
            .thenReturn(Optional.of(existente));

        var resultado = intentoService.registrar(intentoDeTomas(3, 4), "tomas", Rol.ESTUDIANTE);

        assertFalse(resultado.creado());
        verify(intentoRepository, never()).save(any(Intento.class));
    }

    @Test
    void masCorrectasQueTotalEsError() {
        assertThrows(ErrorApi.DatosInvalidos.class,
            () -> intentoService.registrar(intentoDeTomas(5, 4), "tomas", Rol.ESTUDIANTE));
        verify(intentoRepository, never()).save(any(Intento.class));
    }

    @Test
    void estudianteEnviandoElIntentoDeOtroNoTienePermiso() {
        assertThrows(ErrorApi.SinPermiso.class,
            () -> intentoService.registrar(intentoDeTomas(3, 4), "sofia", Rol.ESTUDIANTE));
        verify(intentoRepository, never()).save(any(Intento.class));
    }

    @Test
    void docentePuedeEnviarElIntentoDeUnEstudiante() {
        when(intentoRepository.findByEstudianteNombreUsuarioAndActividadIdAndNumeroIntento(anyString(), anyLong(), anyInt()))
            .thenReturn(Optional.empty());
        when(intentoRepository.save(any(Intento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = intentoService.registrar(intentoDeTomas(3, 4), "ana", Rol.DOCENTE);

        assertTrue(resultado.creado());
        assertEquals("ana", resultado.intento().enviadoPor());
    }
}
