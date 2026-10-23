package cl.duoc.mpazpro.ui.estudiante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.local.relation.UnidadEstudiante
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Una asignatura con las unidades que el estudiante tiene por cursar. */
data class AsignaturaPorCursar(
    val id: Long,
    val nombre: String,
    val colorHex: String,
    val unidades: List<UnidadEstudiante>
)

data class EstudianteUiState(
    val cargando: Boolean = true,
    val sinCurso: Boolean = false,
    val cursoNombre: String = "",
    val asignaturas: List<AsignaturaPorCursar> = emptyList()
)

/** Inicio del estudiante: su curso y lo que tiene por cursar. */
class EstudianteViewModel(
    asignacionRepository: AsignacionRepository,
    usuarioRepository: UsuarioRepository,
    cursoId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstudianteUiState())
    val uiState: StateFlow<EstudianteUiState> = _uiState.asStateFlow()

    init {
        if (cursoId == null) {
            // Un estudiante sin curso no tiene nada asignado
            _uiState.value = EstudianteUiState(cargando = false, sinCurso = true)
        } else {
            viewModelScope.launch {
                val curso = usuarioRepository.curso(cursoId)
                _uiState.update { it.copy(cursoNombre = curso?.nombre.orEmpty()) }
            }
            viewModelScope.launch {
                asignacionRepository.unidadesPorCursar(cursoId).collect { unidades ->
                    _uiState.update { it.copy(cargando = false, asignaturas = agrupar(unidades)) }
                }
            }
        }
    }

    /** La consulta trae una fila por unidad; aquí se juntan por asignatura (mantiene el orden). */
    private fun agrupar(unidades: List<UnidadEstudiante>): List<AsignaturaPorCursar> =
        unidades.groupBy { it.asignaturaId }.map { (asignaturaId, lista) ->
            AsignaturaPorCursar(
                id = asignaturaId,
                nombre = lista.first().asignaturaNombre,
                colorHex = lista.first().colorHex,
                unidades = lista
            )
        }

    companion object {
        fun factory(cursoId: Long?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                EstudianteViewModel(
                    asignacionRepository = app.container.asignacionRepository,
                    usuarioRepository = app.container.usuarioRepository,
                    cursoId = cursoId
                )
            }
        }
    }
}