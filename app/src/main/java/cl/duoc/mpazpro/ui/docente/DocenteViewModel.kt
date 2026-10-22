package cl.duoc.mpazpro.ui.docente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocenteUiState(
    val cursos: List<CursoEntity> = emptyList(),
    val asignaturas: List<AsignaturaEntity> = emptyList(),
    val cursoSeleccionado: CursoEntity? = null,
    val asignaturaSeleccionada: AsignaturaEntity? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class DocenteViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val docenteId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocenteUiState())
    val uiState: StateFlow<DocenteUiState> = _uiState.asStateFlow()

    init {
        cargarCursos()
    }

    private fun cargarCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                usuarioRepository.cursosDelDocente(docenteId).collect { listaCursos ->
                    _uiState.update { it.copy(cursos = listaCursos, cargando = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    fun seleccionarCurso(curso: CursoEntity) {
        _uiState.update { it.copy(cursoSeleccionado = curso, asignaturaSeleccionada = null) }
        viewModelScope.launch {
            usuarioRepository.asignaturasPermitidas(docenteId, curso.id).collect { listaAsignaturas ->
                _uiState.update { it.copy(asignaturas = listaAsignaturas) }
            }
        }
    }

    fun seleccionarAsignatura(asignatura: AsignaturaEntity) {
        _uiState.update { it.copy(asignaturaSeleccionada = asignatura) }
    }

    companion object {
        fun factory(docenteId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MpazApp
                DocenteViewModel(app.container.usuarioRepository, docenteId)
            }
        }
    }
}