package cl.duoc.mpazpro.data

import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Quién está usando la app en este momento. Vive mientras la app está abierta. */
class Sesion {

    private val _usuarioActual = MutableStateFlow<UsuarioEntity?>(null)

    /** El usuario conectado, o null si nadie ha iniciado sesión. */
    val usuarioActual: StateFlow<UsuarioEntity?> = _usuarioActual.asStateFlow()

    fun iniciar(usuario: UsuarioEntity) {
        _usuarioActual.value = usuario
    }

    fun cerrar() {
        _usuarioActual.value = null
    }
}