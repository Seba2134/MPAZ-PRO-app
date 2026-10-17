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
    val cargando: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sesion: Sesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ---------- Eventos que llegan desde la pantalla ----------

    fun onNombreUsuarioChange(valor: String) {
        _uiState.update { it.copy(nombreUsuario = valor, error = null) }
    }

    fun onPasswordChange(valor: String) {
        _uiState.update { it.copy(password = valor, error = null) }
    }

    fun alternarMostrarPassword() {
        _uiState.update { it.copy(mostrarPassword = !it.mostrarPassword) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        if (estado.nombreUsuario.isBlank() || estado.password.isBlank()) {
            _uiState.update { it.copy(error = "Escribe tu usuario y tu contraseña") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            when (val resultado = usuarioRepository.iniciarSesion(estado.nombreUsuario, estado.password)) {
                is ResultadoLogin.Exito -> {
                    _uiState.update { it.copy(cargando = false, password = "") }
                    sesion.iniciar(resultado.usuario)
                }
                ResultadoLogin.CredencialesInvalidas -> {
                    _uiState.update {
                        it.copy(cargando = false, password = "", error = "Usuario y/o contraseña incorrectos")
                    }
                }
                ResultadoLogin.CuentaInactiva -> {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            password = "",
                            error = "Tu cuenta está desactivada. Habla con Jefatura UTP."
                        )
                    }
                }
            }
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