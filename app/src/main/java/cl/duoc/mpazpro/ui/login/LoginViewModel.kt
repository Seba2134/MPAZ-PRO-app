package cl.duoc.mpazpro.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.repository.ResultadoLogin
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Todo lo que la pantalla de login necesita para dibujarse. */
data class LoginUiState(
    val nombreUsuario: String = "",
    val password: String = "",
    val mostrarPassword: Boolean = false,
    val errorUsuario: String? = null,      // error del campo usuario
    val errorPassword: String? = null,     // error del campo contraseña
    val errorGeneral: String? = null,      // "Usuario o contraseña incorrectos"
    val cargando: Boolean = false,
    val intentoEnviar: Boolean = false,    // ¿ya presionó "Entrar" al menos una vez?
    val intentosFallidos: Int = 0          // cada vez que sube, la pantalla se sacude
)

class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sesion: Sesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ---------- Eventos que llegan desde la pantalla ----------

    fun onNombreUsuarioChange(valor: String) {
        _uiState.update {
            it.copy(
                nombreUsuario = valor,
                errorGeneral = null,
                // Solo se valida mientras escribe si ya intentó enviar una vez
                errorUsuario = if (it.intentoEnviar) Validaciones.nombreUsuario(valor) else null
            )
        }
    }

    fun onPasswordChange(valor: String) {
        _uiState.update {
            it.copy(
                password = valor,
                errorGeneral = null,
                errorPassword = if (it.intentoEnviar) Validaciones.password(valor) else null
            )
        }
    }

    fun alternarMostrarPassword() {
        _uiState.update { it.copy(mostrarPassword = !it.mostrarPassword) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        // 1) Validar cada campo
        val errorUsuario = Validaciones.nombreUsuario(estado.nombreUsuario)
        val errorPassword = Validaciones.password(estado.password)

        if (errorUsuario != null || errorPassword != null) {
            _uiState.update {
                it.copy(
                    intentoEnviar = true,
                    errorUsuario = errorUsuario,
                    errorPassword = errorPassword,
                    intentosFallidos = it.intentosFallidos + 1
                )
            }
            return
        }

        // 2) Los campos son válidos: verificar con la base de datos
        _uiState.update { it.copy(cargando = true, intentoEnviar = true, errorGeneral = null) }

        viewModelScope.launch {
            when (val resultado = usuarioRepository.iniciarSesion(estado.nombreUsuario, estado.password)) {
                is ResultadoLogin.Exito -> {
                    _uiState.update { it.copy(cargando = false, password = "") }
                    sesion.iniciar(resultado.usuario)
                }
                ResultadoLogin.CredencialesInvalidas -> mostrarFallo("Usuario o contraseña incorrectos")
                ResultadoLogin.CuentaInactiva -> mostrarFallo("Tu cuenta está desactivada. Habla con Jefatura UTP.")
            }
        }
    }

    private fun mostrarFallo(mensaje: String) {
        _uiState.update {
            it.copy(
                cargando = false,
                password = "",
                errorGeneral = mensaje,
                intentosFallidos = it.intentosFallidos + 1
            )
        }
    }

    // ---------- Cómo se crea este ViewModel ----------
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                LoginViewModel(
                    usuarioRepository = app.container.usuarioRepository,
                    sesion = app.container.sesion
                )
            }
        }
    }
}