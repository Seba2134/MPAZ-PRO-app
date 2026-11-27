package cl.duoc.mpazpro.resultados.controller;

import cl.duoc.mpazpro.resultados.dto.ObservacionActualizacion;
import cl.duoc.mpazpro.resultados.dto.ObservacionRequest;
import cl.duoc.mpazpro.resultados.dto.ObservacionResponse;
import cl.duoc.mpazpro.resultados.service.ObservacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Solo DOCENTE y JEFATURA_UTP (ver SeguridadConfig): el estudiante no ve observaciones. */
@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    private final ObservacionService observacionService;

    public ObservacionController(ObservacionService observacionService) {
        this.observacionService = observacionService;
    }

    /** Recibir. Ejemplo: GET /api/observaciones?estudiante=tomas&unidadTitulo=... · 200 · 404 si no existe. */
    @GetMapping
    public ObservacionResponse buscar(
        @RequestParam("estudiante") String estudiante,
        @RequestParam("unidadTitulo") String unidadTitulo
    ) {
        return observacionService.buscar(estudiante, unidadTitulo);
    }

    /** Enviar: crea la observación. 201 · 409 si ya existe (con su id) · 400. */
    @PostMapping
    public ResponseEntity<ObservacionResponse> crear(@Valid @RequestBody ObservacionRequest datos, Authentication autenticacion) {
        ObservacionResponse creada = observacionService.crear(datos, autenticacion.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** Actualizar: cambia texto y/o validado. 200 · 404 · 400. */
    @PutMapping("/{id}")
    public ObservacionResponse actualizar(
        @PathVariable Long id,
        @Valid @RequestBody ObservacionActualizacion datos,
        Authentication autenticacion
    ) {
        return observacionService.actualizar(id, datos, autenticacion.getName());
    }
}
