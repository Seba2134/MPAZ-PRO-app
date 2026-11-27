package cl.duoc.mpazpro.usuarios.service;

import cl.duoc.mpazpro.usuarios.dto.UsuarioResponse;
import cl.duoc.mpazpro.usuarios.exception.ErrorApi;
import cl.duoc.mpazpro.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas sobre los usuarios (por ahora, solo «quién soy»). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Los datos del usuario dueño del token (su nombre de usuario viene del filtro JWT). */
    @Transactional(readOnly = true)
    public UsuarioResponse yo(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
            .map(UsuarioResponse::desde)
            .orElseThrow(() -> new ErrorApi.NoEncontrado("No existe el usuario " + nombreUsuario));
    }
}
