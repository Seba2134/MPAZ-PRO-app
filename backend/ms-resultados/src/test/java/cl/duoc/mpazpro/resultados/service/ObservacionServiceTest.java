package cl.duoc.mpazpro.resultados.service;

import cl.duoc.mpazpro.resultados.dto.ObservacionActualizacion;
import cl.duoc.mpazpro.resultados.dto.ObservacionRequest;
import cl.duoc.mpazpro.resultados.dto.ObservacionResponse;
import cl.duoc.mpazpro.resultados.exception.ErrorApi;
import cl.duoc.mpazpro.resultados.model.Observacion;
import cl.duoc.mpazpro.resultados.repository.ObservacionRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObservacionServiceTest {

    private final ObservacionRepository repositorio = mock(ObservacionRepository.class);
    private final ObservacionService servicio = new ObservacionService(repositorio);

    private static Observacion existente(boolean validada) {
        Observacion o = new Observacion("tomas", "Unidad 1", "Reconoce los personajes", "ana", 1L);
        if (validada) o.actualizar("Reconoce los personajes", true, "ana", 2L);
        return o;
    }

    @Test
    void crearGuardaUnaObservacionSinValidar() {
        when(repositorio.findByEstudianteNombreUsuarioAndUnidadTitulo("tomas", "Unidad 1")).thenReturn(Optional.empty());
        when(repositorio.save(any(Observacion.class))).thenAnswer(inv -> inv.getArgument(0));

        ObservacionResponse creada = servicio.crear(new ObservacionRequest(" Tomas ", "Unidad 1", "Muy bien"), "ana");

        assertEquals("tomas", creada.estudianteNombreUsuario());
        assertEquals("Muy bien", creada.texto());
        assertEquals("ana", creada.docenteNombreUsuario());
        assertFalse(creada.validado());
    }

    @Test
    void crearUnaQueYaExisteEsConflicto() {
        when(repositorio.findByEstudianteNombreUsuarioAndUnidadTitulo("tomas", "Unidad 1"))
            .thenReturn(Optional.of(existente(false)));

        assertThrows(ErrorApi.Conflicto.class,
            () -> servicio.crear(new ObservacionRequest("tomas", "Unidad 1", "Otra"), "ana"));
        verify(repositorio, never()).save(any(Observacion.class));
    }

    @Test
    void cambiarElTextoQuitaLaValidacion() {
        Observacion o = existente(true);
        when(repositorio.findById(7L)).thenReturn(Optional.of(o));
        when(repositorio.save(any(Observacion.class))).thenAnswer(inv -> inv.getArgument(0));

        ObservacionResponse r = servicio.actualizar(7L, new ObservacionActualizacion("Texto nuevo", true), "carlos");

        assertEquals("Texto nuevo", r.texto());
        assertFalse(r.validado());
        assertEquals("carlos", r.docenteNombreUsuario());
    }

    @Test
    void validarConElMismoTextoQuedaValidada() {
        Observacion o = existente(false);
        when(repositorio.findById(7L)).thenReturn(Optional.of(o));
        when(repositorio.save(any(Observacion.class))).thenAnswer(inv -> inv.getArgument(0));

        ObservacionResponse r = servicio.actualizar(7L, new ObservacionActualizacion("Reconoce los personajes", true), "ana");

        assertTrue(r.validado());
    }

    @Test
    void actualizarUnaQueNoExisteEsNoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ErrorApi.NoEncontrado.class,
            () -> servicio.actualizar(99L, new ObservacionActualizacion("x", false), "ana"));
    }
}
