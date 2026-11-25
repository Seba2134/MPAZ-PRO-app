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
import cl.duoc.mpazpro.data.local.entity.ObservacionDocenteEntity
import cl.duoc.mpazpro.data.local.relation.RendimientoOa
import cl.duoc.mpazpro.data.local.relation.RespuestaDetalle
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.data.repository.ResultadoRepository
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InformeUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    // Encabezado
    val estudiante: String = "",
    val asignatura: String = "",
    val unidad: String = "",
    // Resultados del mejor intento de la prueba final
    val mejorIntento: IntentoEntity? = null,
    val rendimiento: List<RendimientoOa> = emptyList(),
    val detalle: List<RespuestaDetalle> = emptyList(),
    // Observación del docente
    val observacionGuardada: ObservacionDocenteEntity? = null,
    val textoObservacion: String = "",
    val errorObservacion: String? = null,
    val intentoEnviar: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null
) {
    val correctasTotales: Int get() = rendimiento.sumOf { it.correctas }
    val preguntasTotales: Int get() = rendimiento.sumOf { it.total }
    val porcentajeGlobal: Int get() = if (preguntasTotales == 0) 0 else correctasTotales * 100 / preguntasTotales
}

/** El informe por OA de un estudiante: lo ve y lo valida el docente (nunca el estudiante). */
@OptIn(ExperimentalCoroutinesApi::class)
class InformeViewModel(
    private val asignacionRepository: AsignacionRepository,
    private val contenidoRepository: ContenidoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val resultadoRepository: ResultadoRepository,
    private val sesion: Sesion,
    private val asignacionId: Long,
    private val estudianteId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(InformeUiState())
    val uiState: StateFlow<InformeUiState> = _uiState.asStateFlow()

    // El texto se llena una sola vez con lo guardado, para no pisar lo que el docente escribe
    private var textoInicializado = false
    private var unidadId: Long = 0L

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            if (!tieneAcceso()) return@launch

            val asignacion = asignacionRepository.asignacionPorId(asignacionId) ?: return@launch
            val detalle = asignacionRepository.detallePrueba(asignacionId)
            val estudiante = usuarioRepository.usuario(estudianteId)
            if (estudiante == null || estudiante.rol != Rol.ESTUDIANTE || estudiante.cursoId != asignacion.cursoId) {
                _uiState.value = InformeUiState(cargando = false, error = "No se encontró al estudiante en este curso")
                return@launch
            }
            unidadId = asignacion.unidadId
            _uiState.update {
                it.copy(
                    estudiante = estudiante.nombre,
                    asignatura = detalle?.asignaturaNombre.orEmpty(),
                    unidad = detalle?.unidadTitulo.orEmpty()
                )
            }

            val prueba = contenidoRepository.pruebaFinal(unidadId)
            if (prueba == null) {
                _uiState.update { it.copy(cargando = false, error = "Esta unidad todavía no tiene prueba final") }
                return@launch
            }

            val mejorConDetalle = resultadoRepository.mejorIntento(estudianteId, prueba.id).flatMapLatest { mejor ->
                if (mejor == null) flowOf(null to emptyList())
                else resultadoRepository.detalleDelIntento(mejor.id).map { mejor to it }
            }

            combine(
                mejorConDetalle,
                resultadoRepository.rendimientoPorOa(estudianteId, unidadId),
                resultadoRepository.observacion(estudianteId, unidadId)
            ) { (mejor, respuestas), rendimiento, observacion ->
                Triple(mejor to respuestas, rendimiento, observacion)
            }.collect { (mejorYRespuestas, rendimiento, observacion) ->
                val (mejor, respuestas) = mejorYRespuestas
                _uiState.update {
                    val texto = if (!textoInicializado) observacion?.texto.orEmpty() else it.textoObservacion
                    textoInicializado = true
                    it.copy(
                        cargando = false,
                        mejorIntento = mejor,
                        detalle = respuestas,
                        rendimiento = rendimiento,
                        observacionGuardada = observacion,
                        textoObservacion = texto
                    )
                }
            }
        }
    }

    /** Solo el profesor jefe o el docente de esa asignatura en ese curso. */
    private suspend fun tieneAcceso(): Boolean {
        val docente = sesion.usuarioActual.value
        val asignacion = asignacionRepository.asignacionPorId(asignacionId)
        val unidad = asignacion?.let { contenidoRepository.unidad(it.unidadId) }
        val acceso = docente != null && asignacion != null && unidad != null && docente.rol == Rol.DOCENTE &&
            usuarioRepository.tieneAcceso(docente.id, asignacion.cursoId, unidad.asignaturaId)
        if (!acceso) {
            _uiState.update { it.copy(cargando = false, guardando = false, error = "No tienes acceso a este curso") }
        }
        return acceso
    }

    fun onObservacionChange(valor: String) = _uiState.update {
        it.copy(
            textoObservacion = valor,
            errorObservacion = if (it.intentoEnviar) Validaciones.observacion(valor) else null
        )
    }

    fun guardarObservacion() {
        val estado = _uiState.value
        if (estado.guardando) return
        val error = Validaciones.observacion(estado.textoObservacion)
        _uiState.update { it.copy(intentoEnviar = true, errorObservacion = error, mensaje = null) }
        if (error != null) return

        val docenteId = sesion.usuarioActual.value?.id ?: return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            if (!tieneAcceso()) return@launch
            resultadoRepository.guardarObservacion(estudianteId, unidadId, docenteId, estado.textoObservacion.trim())
            _uiState.update { it.copy(guardando = false, mensaje = "Observación guardada") }
        }
    }

    fun validarInforme() {
        val estado = _uiState.value
        if (estado.guardando || estado.observacionGuardada == null) return
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            if (!tieneAcceso()) return@launch
            resultadoRepository.validarInforme(estudianteId, unidadId)
            _uiState.update { it.copy(guardando = false, mensaje = "Informe validado ✓") }
        }
    }

    fun mensajeVisto() = _uiState.update { it.copy(mensaje = null) }

    companion object {
        fun factory(asignacionId: Long, estudianteId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                InformeViewModel(
                    asignacionRepository = app.container.asignacionRepository,
                    contenidoRepository = app.container.contenidoRepository,
                    usuarioRepository = app.container.usuarioRepository,
                    resultadoRepository = app.container.resultadoRepository,
                    sesion = app.container.sesion,
                    asignacionId = asignacionId,
                    estudianteId = estudianteId
                )
            }
        }
    }
}
