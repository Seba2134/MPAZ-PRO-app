package cl.duoc.mpazpro.ui.utp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.repository.ResultadoCuenta
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CrearUsuarioUiState(
    val run: String = "",
    val nombre: String = "",
    val correo: String = "",
    val nombreUsuario: String = "",
    val password: String = "",
    val rolSeleccionado: Rol = Rol.ESTUDIANTE,
    val errorUsuario: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val exito: Boolean = false,
    val cargando: Boolean = false
)

class CrearUsuarioViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearUsuarioUiState())
    val uiState: StateFlow<CrearUsuarioUiState> = _uiState.asStateFlow()

    fun onRunChange(v: String) = _uiState.update { it.copy(run = v) }
    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v) }
    fun onCorreoChange(v: String) = _uiState.update { it.copy(correo = v) }
    fun onUsuarioChange(v: String) = _uiState.update { it.copy(nombreUsuario = v, errorUsuario = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorPassword = null) }
    fun onRolChange(v: Rol) = _uiState.update { it.copy(rolSeleccionado = v) }

    fun guardarUsuario() {
        val errUser = Validaciones.nombreUsuario(_uiState.value.nombreUsuario)
        val errPass = Validaciones.password(_uiState.value.password)

        if (errUser != null || errPass != null) {
            _uiState.update { it.copy(errorUsuario = errUser, errorPassword = errPass) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            try {
                // Se invoca la función del repositorio respetando el flujo de resultado
                val resultado = usuarioRepository.crearUsuario(
                    creadorId = 1L, // ID por defecto de la sesión UTP activa
                    run = _uiState.value.run,
                    nombre = _uiState.value.nombre,
                    correo = _uiState.value.correo,
                    nombreUsuario = _uiState.value.nombreUsuario,
                    password = _uiState.value.password,
                    rol = _uiState.value.rolSeleccionado,
                    cursoId = null
                )

                when (resultado) {
                    ResultadoCuenta.OK -> {
                        _uiState.update { it.copy(cargando = false, exito = true) }
                    }
                    ResultadoCuenta.NOMBRE_USUARIO_EN_USO -> {
                        _uiState.update { it.copy(cargando = false, errorUsuario = "El nombre de usuario ya existe") }
                    }
                    else -> {
                        _uiState.update { it.copy(cargando = false, errorGeneral = "Error al crear la cuenta: $resultado") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = e.message ?: "Error al registrar el usuario"
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MpazApp
                CrearUsuarioViewModel(app.container.usuarioRepository)
            }
        }
    }
}