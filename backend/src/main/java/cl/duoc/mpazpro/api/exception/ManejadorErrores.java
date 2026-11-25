package cl.duoc.mpazpro.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte los errores en respuestas JSON simples: {"error": "..."}. */
@RestControllerAdvice
public class ManejadorErrores {

    /** 400 con la lista de campos que no pasaron la validación. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(error -> campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", "Datos inválidos");
        cuerpo.put("campos", campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** 400 cuando el JSON viene mal formado o con un valor que no corresponde (ej: un tipo inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonIlegible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "El cuerpo de la petición no es válido"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> parametroFaltante(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Falta el parámetro " + e.getParameterName()));
    }

    /** 401, 403, 404 o 400 según el error que lanzó el servicio. */
    @ExceptionHandler(ErrorApi.class)
    public ResponseEntity<Map<String, Object>> errorApi(ErrorApi e) {
        HttpStatus estado = e.getEstado();
        return ResponseEntity.status(estado).body(Map.of("error", e.getMessage()));
    }
}
