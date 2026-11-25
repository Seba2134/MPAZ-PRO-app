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

    private val _token = MutableStateFlow<String?>(null)

    /**
     * El JWT que entregó el servidor, o null si no hay (servidor apagado o token vencido).
     * Solo vive en memoria: nunca se guarda en disco ni en Room.
     */
    val token: StateFlow<String?> = _token.asStateFlow()

    fun iniciar(usuario: UsuarioEntity) {
        _usuarioActual.value = usuario
    }

    fun guardarToken(token: String) {
        _token.value = token
    }

    /** Se usa cuando el servidor responde 401 (token vencido o inválido). */
    fun borrarToken() {
        _token.value = null
    }

    fun cerrar() {
        _token.value = null
        _usuarioActual.value = null
    }
}