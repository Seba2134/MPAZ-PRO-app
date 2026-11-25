package cl.duoc.mpazpro.ui.docente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ResultadoAsignacion
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Una unidad que el docente puede asignar, y si su curso ya la tiene. */
data class UnidadAsignable(
    val unidad: UnidadEntity,
    val asignada: Boolean
)

/** Una asignatura que el docente puede ver en el curso, con sus unidades disponibles. */
data class AsignaturaConUnidades(
    val asignatura: AsignaturaEntity,
    val unidades: List<UnidadAsignable>
)

data class AsignarUnidadesUiState(
    val cargando: Boolean = true,
    val cursos: List<CursoEntity> = emptyList(),
    val cursoId: Long? = null,
    val asignaturas: List<AsignaturaConUnidades> = emptyList(),
    val asignandoId: Long? = null,     // la unidad que se está asignando ahora
    val mensaje: String? = null
)

/** El docente asigna a su curso las unidades que UTP ya autorizó. */
@OptIn(ExperimentalCoroutinesApi::class)
class AsignarUnidadesViewModel(
    private val asignacionRepository: AsignacionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val sesion: Sesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsignarUnidadesUiState())
    val uiState: StateFlow<AsignarUnidadesUiState> = _uiState.asStateFlow()

    private val cursoElegido = MutableStateFlow<Long?>(null)

    init {
        val docenteId = sesion.usuarioActual.value?.id
        if (docenteId == null) {
            _uiState.value = AsignarUnidadesUiState(cargando = false)
        } else {
            viewModelScope.launch {
                usuarioRepository.cursosDelDocente(docenteId).collect { cursos ->
                    _uiState.update { it.copy(cursos = cursos, cargando = false) }
                    // Si tiene un solo curso, queda elegido de inmediato
                    if (cursoElegido.value == null && cursos.size == 1) elegirCurso(cursos.first().id)
                }
            }
            viewModelScope.launch {
                cursoElegido
                    .flatMapLatest { cursoId ->
                        if (cursoId == null) flowOf(emptyList()) else asignaturasConUnidades(docenteId, cursoId)
                    }
                    .collect { lista -> _uiState.update { it.copy(asignaturas = lista) } }
            }
        }
    }

    /** Por cada asignatura permitida, sus unidades disponibles marcando las que el curso ya tiene. */
    private fun asignaturasConUnidades(docenteId: Long, cursoId: Long): Flow<List<AsignaturaConUnidades>> =
        usuarioRepository.asignaturasPermitidas(docenteId, cursoId).flatMapLatest { asignaturas ->
            if (asignaturas.isEmpty()) {
                flowOf(emptyList())
            } else {
                val unidadesPorAsignatura = combine(
                    asignaturas.map { asignatura ->
                        asignacionRepository.unidadesDisponibles(asignatura.id).map { asignatura to it }
                    }
                ) { it.toList() }

                combine(unidadesPorAsignatura, asignacionRepository.asignacionesDelCurso(cursoId)) { pares, asignaciones ->
                    val yaAsignadas = asignaciones.map { it.unidadId }.toSet()
                    pares.map { (asignatura, unidades) ->
                        AsignaturaConUnidades(
                            asignatura = asignatura,
                            unidades = unidades.map { UnidadAsignable(it, it.id in yaAsignadas) }
                        )
                    }
                }
            }
        }

    fun elegirCurso(cursoId: Long) {
        cursoElegido.value = cursoId
        _uiState.update { it.copy(cursoId = cursoId, mensaje = null) }
    }

    fun asignar(unidadId: Long) {
        val estado = _uiState.value
        val cursoId = estado.cursoId ?: return
        if (estado.asignandoId != null) return
        val docenteId = sesion.usuarioActual.value?.id ?: run {
            _uiState.update { it.copy(mensaje = "Tu sesión terminó. Vuelve a iniciar sesión.") }
            return
        }
        val cursoNombre = estado.cursos.firstOrNull { it.id == cursoId }?.nombre.orEmpty()

        _uiState.update { it.copy(asignandoId = unidadId, mensaje = null) }
        viewModelScope.launch {
            val mensaje = try {
                when (asignacionRepository.asignarUnidad(docenteId, cursoId, unidadId)) {
                    ResultadoAsignacion.ASIGNADA -> "Unidad asignada a $cursoNombre"
                    ResultadoAsignacion.YA_ASIGNADA -> "El curso ya tiene esta unidad"
                    ResultadoAsignacion.SIN_ACCESO -> "No haces esa asignatura en este curso"
                    ResultadoAsignacion.NO_DISPONIBLE -> "UTP aún no autoriza esta unidad"
                }
            } catch (e: Exception) {
                "No se pudo asignar la unidad. Intenta de nuevo."
            }
            _uiState.update { it.copy(asignandoId = null, mensaje = mensaje) }
        }
    }

    fun mensajeVisto() = _uiState.update { it.copy(mensaje = null) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                AsignarUnidadesViewModel(
                    asignacionRepository = app.container.asignacionRepository,
                    usuarioRepository = app.container.usuarioRepository,
                    sesion = app.container.sesion
                )
            }
        }
    }
}
