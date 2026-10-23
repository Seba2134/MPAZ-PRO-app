package cl.duoc.mpazpro.ui.utp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.data.repository.ResultadoCurso
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que la pantalla muestra de cada curso. */
data class CursoResumen(
    val id: Long,
    val nombre: String,
    val profesorJefeNombre: String,
    val cantidadEstudiantes: Int,
    val docentePorAsignatura: Map<Long, Long>   // asignaturaId -> docenteId
)

data class GestionCursosUiState(
    // Datos de la base de datos
    val cargando: Boolean = true,
    val cursos: List<CursoResumen> = emptyList(),
    val asignaturas: List<AsignaturaEntity> = emptyList(),
    val docentes: List<UsuarioEntity> = emptyList(),

    // Formulario "Nuevo curso"
    val nombreNuevo: String = "",
    val profesorJefeId: Long? = null,
    val errorNombre: String? = null,
    val errorProfesorJefe: String? = null,
    val intentoEnviar: Boolean = false,
    val creando: Boolean = false,

    // Mensaje para la persona: "Curso creado", "No se puede eliminar…"
    val mensaje: String? = null
)

/** Jefatura UTP crea y elimina cursos, y decide qué docente hace cada asignatura. */
class GestionCursosViewModel(
    private val usuarioRepository: UsuarioRepository,
    contenidoRepository: ContenidoRepository,
    private val sesion: Sesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionCursosUiState())
    val uiState: StateFlow<GestionCursosUiState> = _uiState.asStateFlow()

    init {
        // Se juntan 4 consultas; si cualquiera cambia, la pantalla se actualiza sola
        viewModelScope.launch {
            combine(
                usuarioRepository.cursosConEstudiantes(),
                usuarioRepository.perfilesPorRol(Rol.DOCENTE),
                contenidoRepository.asignaturas(),
                usuarioRepository.cargaAcademicaCompleta()
            ) { cursos, docentes, asignaturas, carga ->
                val resumenes = cursos.map { cursoConEstudiantes ->
                    val curso = cursoConEstudiantes.curso
                    CursoResumen(
                        id = curso.id,
                        nombre = curso.nombre,
                        profesorJefeNombre = docentes.firstOrNull { it.id == curso.profesorJefeId }?.nombre
                            ?: "Sin profesor jefe",
                        cantidadEstudiantes = cursoConEstudiantes.estudiantes.count { it.rol == Rol.ESTUDIANTE },
                        docentePorAsignatura = carga
                            .filter { it.cursoId == curso.id }
                            .associate { it.asignaturaId to it.docenteId }
                    )
                }
                Triple(resumenes, docentes, asignaturas)
            }.collect { (resumenes, docentes, asignaturas) ->
                _uiState.update {
                    it.copy(cargando = false, cursos = resumenes, docentes = docentes, asignaturas = asignaturas)
                }
            }
        }
    }

    // ---------- Formulario "Nuevo curso" ----------

    fun onNombreChange(valor: String) = _uiState.update { revalidar(it.copy(nombreNuevo = valor)) }

    fun onProfesorJefeChange(docenteId: Long) = _uiState.update { revalidar(it.copy(profesorJefeId = docenteId)) }

    fun crearCurso() {
        val estado = _uiState.value
        if (estado.creando) return

        val validado = conErrores(estado.copy(intentoEnviar = true, mensaje = null))
        if (validado.errorNombre != null || validado.errorProfesorJefe != null) {
            _uiState.value = validado
            return
        }

        _uiState.value = validado.copy(creando = true)
        viewModelScope.launch {
            val nombre = validado.nombreNuevo.trim()
            val resultado = usuarioRepository.crearCurso(
                creadorId = usuarioActualId(),
                nombre = nombre,
                profesorJefeId = validado.profesorJefeId!!
            )
            _uiState.update {
                when (resultado) {
                    ResultadoCurso.OK -> it.copy(
                        creando = false,
                        nombreNuevo = "",
                        profesorJefeId = null,
                        intentoEnviar = false,
                        errorNombre = null,
                        errorProfesorJefe = null,
                        mensaje = "Curso «$nombre» creado"
                    )
                    ResultadoCurso.NOMBRE_EN_USO -> it.copy(
                        creando = false,
                        errorNombre = "Ya existe un curso con ese nombre"
                    )
                    else -> it.copy(creando = false, mensaje = textoDe(resultado))
                }
            }
        }
    }

    // ---------- Acciones sobre un curso ----------

    /** [docenteId] null = la asignatura queda sin profesor de asignatura (la ve solo el profesor jefe). */
    fun cambiarDocente(cursoId: Long, asignaturaId: Long, docenteId: Long?) {
        viewModelScope.launch {
            val resultado = usuarioRepository.cambiarDocenteDeAsignatura(
                creadorId = usuarioActualId(),
                cursoId = cursoId,
                asignaturaId = asignaturaId,
                docenteId = docenteId
            )
            if (resultado != ResultadoCurso.OK) _uiState.update { it.copy(mensaje = textoDe(resultado)) }
        }
    }

    fun eliminarCurso(curso: CursoResumen) {
        viewModelScope.launch {
            val resultado = usuarioRepository.eliminarCurso(creadorId = usuarioActualId(), cursoId = curso.id)
            val mensaje = if (resultado == ResultadoCurso.OK) "Curso «${curso.nombre}» eliminado" else textoDe(resultado)
            _uiState.update { it.copy(mensaje = mensaje) }
        }
    }

    fun mensajeVisto() = _uiState.update { it.copy(mensaje = null) }

    // ---------- Ayudantes ----------

    private fun usuarioActualId(): Long = sesion.usuarioActual.value?.id ?: 0L

    private fun revalidar(estado: GestionCursosUiState): GestionCursosUiState =
        if (estado.intentoEnviar) conErrores(estado) else estado

    private fun conErrores(estado: GestionCursosUiState): GestionCursosUiState = estado.copy(
        errorNombre = Validaciones.nombreCurso(estado.nombreNuevo),
        errorProfesorJefe = Validaciones.profesorJefe(estado.profesorJefeId)
    )

    private fun textoDe(resultado: ResultadoCurso): String = when (resultado) {
        ResultadoCurso.OK -> "Listo"
        ResultadoCurso.SIN_PERMISO -> "Solo Jefatura UTP puede administrar cursos"
        ResultadoCurso.DATOS_INVALIDOS -> "Revisa los datos e intenta de nuevo"
        ResultadoCurso.NOMBRE_EN_USO -> "Ya existe un curso con ese nombre"
        ResultadoCurso.TIENE_ESTUDIANTES -> "No se puede eliminar: el curso tiene estudiantes"
        ResultadoCurso.NO_ENCONTRADO -> "El curso ya no existe"
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                GestionCursosViewModel(
                    usuarioRepository = app.container.usuarioRepository,
                    contenidoRepository = app.container.contenidoRepository,
                    sesion = app.container.sesion
                )
            }
        }
    }
}