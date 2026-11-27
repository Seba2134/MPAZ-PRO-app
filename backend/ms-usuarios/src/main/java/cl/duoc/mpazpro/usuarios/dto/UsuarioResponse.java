package cl.duoc.mpazpro.usuarios.dto;

import cl.duoc.mpazpro.usuarios.model.Usuario;

/** Los datos públicos del dueño de un token (nunca la clave ni su hash). */
public record UsuarioResponse(
    String nombreUsuario,
    String nombre,
    String rol
) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getNombreUsuario(), usuario.getNombre(), usuario.getRol().name());
    }
}
