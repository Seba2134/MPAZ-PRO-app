package cl.duoc.mpazpro.resultados.exception;

import org.springframework.http.HttpStatus;

/**
 * Error de la API con su código HTTP. Los servicios lanzan una de sus subclases
 * y ManejadorErrores la convierte en {"error": "..."}.
 */
public abstract class ErrorApi extends RuntimeException {

    private final HttpStatus estado;

    protected ErrorApi(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    /** 409: ya existe (por ejemplo, la observación de ese estudiante en esa unidad). Lleva el id existente. */
    public static class Conflicto extends ErrorApi {
        private final Long idExistente;

        public Conflicto(String mensaje, Long idExistente) {
            super(HttpStatus.CONFLICT, mensaje);
            this.idExistente = idExistente;
        }

        public Long getIdExistente() {
            return idExistente;
        }
    }

    /** 403: quien envía no puede hacer esto (por ejemplo, un estudiante enviando el intento de otro). */
    public static class SinPermiso extends ErrorApi {
        public SinPermiso(String mensaje) {
            super(HttpStatus.FORBIDDEN, mensaje);
        }
    }

    /** 404: lo pedido no existe. */
    public static class NoEncontrado extends ErrorApi {
        public NoEncontrado(String mensaje) {
            super(HttpStatus.NOT_FOUND, mensaje);
        }
    }

    /** 400: datos que pasan la validación de campos pero no tienen sentido (correctas > total). */
    public static class DatosInvalidos extends ErrorApi {
        public DatosInvalidos(String mensaje) {
            super(HttpStatus.BAD_REQUEST, mensaje);
        }
    }
}
