package cl.duoc.mpazpro.usuarios.dto;

/** El token y los datos básicos de quien inició sesión. [expiraEn] es epoch en milisegundos. */
public record LoginResponse(
    String token,
    String rol,
    String nombre,
    long expiraEn
) {
}
