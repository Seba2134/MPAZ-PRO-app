package cl.duoc.mpazpro.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Para saber si el servidor está encendido. No necesita token. */
@RestController
public class SaludController {

    @GetMapping("/api/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok");
    }
}
