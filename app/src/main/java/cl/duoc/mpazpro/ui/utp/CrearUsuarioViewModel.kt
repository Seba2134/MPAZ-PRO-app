package cl.duoc.mpazpro.ui.utp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.repository.ResultadoCuenta
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Todo lo que el formulario "Crear usuario" necesita para dibujarse. */
data class CrearUsuarioUiState(
    // Campos del formulario
    val run: String = "",
    val nombre: String = "",
    val correo: String = "",
    val nombreUsuario: String = "",
    val password: String = "",
    val mostrarPassword: Boolean = false,
    val rolSeleccionado: Rol = Rol.ESTUDIANTE,

    // Cursos disponibles (vienen de la base de datos)
    val cursos: List<CursoEntity> = emptyList(),
    val cursoSeleccionadoId: Long? = null,

    // Errores por campo
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorUsuario: String? = null,
    val errorPassword: String? = null,
    val errorCurso: String? = null,
    val errorGeneral: String? = null,

    // Estado del envío
    val intentoEnviar: Boolean = false,
    val cargando: Boolean = false,
    val exito: Boolean = false
)

class CrearUsuarioViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sesion: Sesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearUsuarioUiState())
    val uiState: StateFlow<CrearUsuarioUiState> = _uiState.asStateFlow()

    init {
        cargarCursos()
    }

    /** Observa los cursos. Si existe uno solo, lo deja seleccionado de inmediato. */
    private fun cargarCursos() {
        viewModelScope.launch {
            usuarioRepository.todosLosCursos().collect { cursos ->
                _uiState.update { estado ->
                    estado.copy(
                        cursos = cursos,
                        cursoSeleccionadoId = estado.cursoSeleccionadoId ?: cursos.singleOrNull()?.id
                    )
                }
            }
        }
    }

    // ---------- Eventos que llegan desde la pantalla ----------
    // Después del primer intento de guardar, cada campo se valida mientras se escribe.

    fun onRunChange(valor: String) {
        _uiState.update { it.copy(run = valor) }
    }

    fun onNombreChange(valor: String) {
        _uiState.update {
            it.copy(nombre = valor, errorNombre = if (it.intentoEnviar) Validaciones.nombre(valor) else null)
        }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update {
            it.copy(correo = valor, errorCorreo = if (it.intentoEnviar) Validaciones.correo(valor) else null)
        }
    }

    fun onUsuarioChange(valor: String) {
        _uiState.update {
            it.copy(
                nombreUsuario = valor,
                errorUsuario = if (it.intentoEnviar) Validaciones.nombreUsuario(valor) else null
            )
        }
    }

    fun onPasswordChange(valor: String) {
        _uiState.update {
            it.copy(password = valor, errorPassword = if (it.intentoEnviar) Validaciones.password(valor) else null)
        }
    }

    fun alternarMostrarPassword() {
        _uiState.update { it.copy(mostrarPassword = !it.mostrarPassword) }
    }

    fun onRolChange(rol: Rol) {
        _uiState.update {
            it.copy(
                rolSeleccionado = rol,
                errorCurso = if (it.intentoEnviar) Validaciones.cursoDelEstudiante(rol, it.cursoSeleccionadoId) else null
            )
        }
    }

    fun onCursoChange(cursoId: Long) {
        _uiState.update { it.copy(cursoSeleccionadoId = cursoId, errorCurso = null) }
    }

    // ---------- Guardar ----------

    fun guardarUsuario() {
        val estado = _uiState.value
        if (estado.cargando) return

        // 1) Validar todos los campos de una vez
        val conErrores = estado.copy(
            intentoEnviar = true,
            errorNombre = Validaciones.nombre(estado.nombre),
            errorCorreo = Validaciones.correo(estado.correo),
            errorUsuario = Validaciones.nombreUsuario(estado.nombreUsuario),
            errorPassword = Validaciones.password(estado.password),
            errorCurso = Validaciones.cursoDelEstudiante(estado.rolSeleccionado, estado.cursoSeleccionadoId),
            errorGeneral = null
        )

        val hayErrores = listOf(
            conErrores.errorNombre,
            conErrores.errorCorreo,
            conErrores.errorUsuario,
            conErrores.errorPassword,
            conErrores.errorCurso
        ).any { it != null }

        if (hayErrores) {
            _uiState.value = conErrores
            return
        }

        // 2) Quién está creando la cuenta: el usuario de la sesión, no un id fijo
        val creadorId = sesion.usuarioActual.value?.id
        if (creadorId == null) {
            _uiState.value = conErrores.copy(errorGeneral = "Tu sesión terminó. Vuelve a iniciar sesión.")
            return
        }

        _uiState.value = conErrores.copy(cargando = true)

        // 3) Guardar
        viewModelScope.launch {
            val resultado = try {
                usuarioRepository.crearUsuario(
                    creadorId = creadorId,
                    run = estado.run,
                    nombre = estado.nombre,
                    correo = estado.correo,
                    nombreUsuario = estado.nombreUsuario,
                    password = estado.password,
                    rol = estado.rolSeleccionado,
                    cursoId = if (estado.rolSeleccionado == Rol.ESTUDIANTE) estado.cursoSeleccionadoId else null
                )
            } catch (e: Exception) {
                null
            }

            _uiState.update {
                when (resultado) {
                    ResultadoCuenta.OK ->
                        it.copy(cargando = false, exito = true)
                    ResultadoCuenta.NOMBRE_USUARIO_EN_USO ->
                        it.copy(cargando = false, errorUsuario = "Ese nombre de usuario ya existe")
                    ResultadoCuenta.CURSO_REQUERIDO ->
                        it.copy(cargando = false, errorCurso = "Elige el curso del estudiante")
                    ResultadoCuenta.SIN_PERMISO ->
                        it.copy(cargando = false, errorGeneral = "Solo Jefatura UTP puede crear cuentas")
                    ResultadoCuenta.DATOS_INVALIDOS ->
                        it.copy(cargando = false, errorGeneral = "Revisa los datos ingresados")
                    ResultadoCuenta.NO_ENCONTRADO, null ->
                        it.copy(cargando = false, errorGeneral = "No se pudo crear la cuenta. Intenta de nuevo.")
                }
            }
        }
    }

    // ---------- Cómo se crea este ViewModel ----------

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                CrearUsuarioViewModel(
                    usuarioRepository = app.container.usuarioRepository,
                    sesion = app.container.sesion
                )
            }
        }
    }
}