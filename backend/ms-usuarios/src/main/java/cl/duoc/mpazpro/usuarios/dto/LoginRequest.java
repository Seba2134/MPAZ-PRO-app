package cl.duoc.mpazpro.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

/** Lo que manda la app para iniciar sesión. */
public record LoginRequest(
    @NotBlank(message = "Escribe el usuario") String nombreUsuario,
    @NotBlank(message = "Escribe la contraseña") String password
) {
}
