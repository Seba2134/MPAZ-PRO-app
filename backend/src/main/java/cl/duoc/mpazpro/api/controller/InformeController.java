package cl.duoc.mpazpro.api.controller;

import cl.duoc.mpazpro.api.dto.RendimientoOaResponse;
import cl.duoc.mpazpro.api.service.InformeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Solo DOCENTE y JEFATURA_UTP (ver SeguridadConfig): el estudiante no tiene acceso a informes. */
@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    /** Ejemplo: GET /api/informes/estudiante/tomas?unidadTitulo=Leemos cuentos */
    @GetMapping("/estudiante/{nombreUsuario}")
    public List<RendimientoOaResponse> porEstudiante(
        @PathVariable String nombreUsuario,
        @RequestParam("unidadTitulo") String unidadTitulo
    ) {
        return informeService.rendimientoPorOa(nombreUsuario, unidadTitulo);
    }
}
