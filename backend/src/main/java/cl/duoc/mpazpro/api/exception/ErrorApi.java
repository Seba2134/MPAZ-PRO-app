package cl.duoc.mpazpro.api.exception;

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

    /** 401: usuario o contraseña incorrectos, o cuenta desactivada. */
    public static class CredencialesInvalidas extends ErrorApi {
        public CredencialesInvalidas(String mensaje) {
            super(HttpStatus.UNAUTHORIZED, mensaje);
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
