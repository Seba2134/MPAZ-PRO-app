package cl.duoc.mpazpro.ui.docente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.IntentoEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.data.repository.ResultadoRepository
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import cl.duoc.mpazpro.domain.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Cómo va un estudiante con la prueba final. */
enum class EstadoRendicion(val nombreVisible: String) {
    SIN_RENDIR("Sin rendir"),
    EN_CURSO("En curso"),
    RENDIDA("Rendida")
}

/** Una fila del avance: un estudiante, su estado, intentos usados y mejor puntaje. */
data class AvanceEstudiante(
    val estudianteId: Long,
    val nombre: String,
    val estado: EstadoRendicion,
    val intentosUsados: Int,
    val mejorPuntaje: String?    // "3/4", o null si no ha terminado ninguno
)

data class AvanceUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val unidad: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val intentosMax: Int = 0,
    val estudiantes: List<AvanceEstudiante> = emptyList()
) {
    val rindieron: Int get() = estudiantes.count { it.estado == EstadoRendicion.RENDIDA }
}

/** El docente ve quién rindió la prueba final de una unidad en su curso. */
class AvanceViewModel(
    private val asignacionRepository: AsignacionRepository,
    private val contenidoRepository: ContenidoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val resultadoRepository: ResultadoRepository,
    private val sesion: Sesion,
    private val asignacionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvanceUiState())
    val uiState: StateFlow<AvanceUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val docente = sesion.usuarioActual.value
            val asignacion = asignacionRepository.asignacionPorId(asignacionId)
            val detalle = asignacionRepository.detallePrueba(asignacionId)
            val unidad = asignacion?.let { contenidoRepository.unidad(it.unidadId) }

            if (docente == null || asignacion == null || detalle == null || unidad == null) {
                _uiState.value = AvanceUiState(cargando = false, error = "No se encontró esta prueba")
                return@launch
            }
            // Permiso: solo el profesor jefe o el docente de esa asignatura en ese curso
            if (docente.rol != Rol.DOCENTE ||
                !usuarioRepository.tieneAcceso(docente.id, asignacion.cursoId, unidad.asignaturaId)
            ) {
                _uiState.value = AvanceUiState(cargando = false, error = "No tienes acceso a este curso")
                return@launch
            }

            _uiState.update {
                it.copy(
                    unidad = detalle.unidadTitulo,
                    asignatura = detalle.asignaturaNombre,
                    curso = detalle.cursoNombre,
                    intentosMax = asignacion.intentosMaxPrueba
                )
            }

            val prueba = contenidoRepository.pruebaFinal(unidad.id)
            if (prueba == null) {
                _uiState.update { it.copy(cargando = false, error = "Esta unidad todavía no tiene prueba final") }
                return@launch
            }

            combine(
                usuarioRepository.cursoConEstudiantes(asignacion.cursoId),
                resultadoRepository.intentosDeActividad(prueba.id)
            ) { curso, intentos ->
                val estudiantes = curso?.estudiantes.orEmpty().filter { it.rol == Rol.ESTUDIANTE && it.activo }
                val porEstudiante = intentos.groupBy { it.estudianteId }
                estudiantes.map { filaDe(it, porEstudiante[it.id].orEmpty()) }
            }.collect { filas ->
                _uiState.update { it.copy(cargando = false, estudiantes = filas) }
            }
        }
    }

    private fun filaDe(estudiante: UsuarioEntity, intentos: List<IntentoEntity>): AvanceEstudiante {
        val terminados = intentos.filter { it.fechaFin != null }
        // Mismo criterio que el informe: más correctas y, si empatan, el más reciente
        val mejor = terminados.maxWithOrNull(compareBy<IntentoEntity> { it.correctas }.thenBy { it.numeroIntento })
        val estado = when {
            terminados.isNotEmpty() -> EstadoRendicion.RENDIDA
            intentos.isNotEmpty() -> EstadoRendicion.EN_CURSO
            else -> EstadoRendicion.SIN_RENDIR
        }
        return AvanceEstudiante(
            estudianteId = estudiante.id,
            nombre = estudiante.nombre,
            estado = estado,
            intentosUsados = intentos.size,
            mejorPuntaje = mejor?.let { "${it.correctas}/${it.totalPreguntas}" }
        )
    }

    companion object {
        fun factory(asignacionId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                AvanceViewModel(
                    asignacionRepository = app.container.asignacionRepository,
                    contenidoRepository = app.container.contenidoRepository,
                    usuarioRepository = app.container.usuarioRepository,
                    resultadoRepository = app.container.resultadoRepository,
                    sesion = app.container.sesion,
                    asignacionId = asignacionId
                )
            }
        }
    }
}
