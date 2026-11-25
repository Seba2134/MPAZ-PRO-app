package cl.duoc.mpazpro.ui.estudiante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.IntentoEntity
import cl.duoc.mpazpro.data.local.relation.PreguntaConOpciones
import cl.duoc.mpazpro.data.local.relation.RespuestaDetalle
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.data.repository.InicioIntento
import cl.duoc.mpazpro.data.repository.ResultadoRepository
import cl.duoc.mpazpro.data.repository.SincronizacionRepository
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.domain.model.TipoActividad
import cl.duoc.mpazpro.util.AudioPlayerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** En qué momento de la actividad está el estudiante. */
sealed interface Fase {
    data object Cargando : Fase
    data class Bloqueada(val mensaje: String) : Fase
    data object Respondiendo : Fase
    data class Terminada(val correctas: Int, val total: Int) : Fase
}

data class RendirActividadUiState(
    val fase: Fase = Fase.Cargando,
    val titulo: String = "",
    val instruccion: String = "",
    val esPruebaFinal: Boolean = false,
    val preguntas: List<PreguntaConOpciones> = emptyList(),
    val indiceActual: Int = 0,
    val respuestas: Map<Long, Long> = emptyMap(),   // preguntaId → opción elegida
    val aciertos: Map<Long, Boolean> = emptyMap(),  // preguntaId → ¿fue correcta?
    val guardando: Boolean = false,
    val reproduciendoAudio: Boolean = false,
    val mostrarDetalle: Boolean = false,
    val detalle: List<RespuestaDetalle> = emptyList(),
    val error: String? = null
) {
    val preguntaActual: PreguntaConOpciones? get() = preguntas.getOrNull(indiceActual)
    val esUltima: Boolean get() = indiceActual == preguntas.lastIndex

    /** La opción que eligió en la pregunta que está en pantalla (null si aún no responde). */
    val opcionElegidaActual: Long? get() = preguntaActual?.let { respuestas[it.pregunta.id] }
}

/** Resolver un desafío o rendir la prueba final, una pregunta por pantalla. */
class RendirActividadViewModel(
    private val contenidoRepository: ContenidoRepository,
    private val asignacionRepository: AsignacionRepository,
    private val resultadoRepository: ResultadoRepository,
    private val sincronizacionRepository: SincronizacionRepository,
    private val sesion: Sesion,
    private val reproductor: AudioPlayerHelper,
    private val alcanceApp: CoroutineScope,
    private val actividadId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(RendirActividadUiState())
    val uiState: StateFlow<RendirActividadUiState> = _uiState.asStateFlow()

    private var intento: IntentoEntity? = null
    private var mostrarRespuestasAlTerminar: Boolean = true

    init {
        iniciar()
    }

    private fun iniciar() {
        viewModelScope.launch {
            val estudiante = sesion.usuarioActual.value
            val cursoId = estudiante?.cursoId
            val actividad = contenidoRepository.actividad(actividadId)
            val unidad = actividad?.let { contenidoRepository.unidad(it.unidadId) }

            if (estudiante == null || cursoId == null || actividad == null ||
                unidad?.estado != EstadoContenido.PUBLICADO
            ) {
                bloquear(MENSAJE_NO_DISPONIBLE)
                return@launch
            }

            val esPrueba = actividad.tipo == TipoActividad.PRUEBA_FINAL
            _uiState.update {
                it.copy(titulo = actividad.titulo, instruccion = actividad.instruccion, esPruebaFinal = esPrueba)
            }

            // Las preguntas se leen antes de crear el intento: sin preguntas no hay nada que rendir
            val preguntas = contenidoRepository.preguntasConOpciones(actividadId).first()
                .map { it.copy(opciones = it.opciones.sortedBy { opcion -> opcion.orden }) }
            if (preguntas.isEmpty()) {
                bloquear("Esta actividad todavía no tiene preguntas")
                return@launch
            }

            mostrarRespuestasAlTerminar = !esPrueba ||
                asignacionRepository.asignacion(cursoId, actividad.unidadId)?.mostrarRespuestas == true

            when (val inicio = resultadoRepository.iniciarActividad(estudiante.id, cursoId, actividad)) {
                is InicioIntento.Nuevo -> {
                    intento = inicio.intento
                    _uiState.update { it.copy(fase = Fase.Respondiendo, preguntas = preguntas) }
                }

                is InicioIntento.Retomar -> {
                    intento = inicio.intento
                    retomar(inicio.intento, preguntas)
                }

                InicioIntento.NoAsignada -> bloquear(MENSAJE_NO_DISPONIBLE)
                is InicioIntento.AunNoDisponible -> bloquear("La prueba abre el ${fechaCorta(inicio.desde)}")
                InicioIntento.Cerrada -> bloquear("La prueba ya cerró")
                is InicioIntento.SinIntentos -> bloquear("Ya usaste tus ${inicio.maximo} intentos")
            }
        }
    }

    /** Recupera lo que ya respondió y lo deja en la primera pregunta sin responder. */
    private suspend fun retomar(intento: IntentoEntity, preguntas: List<PreguntaConOpciones>) {
        val anteriores = resultadoRepository.respuestasDelIntento(intento.id).first()
        val respuestas = anteriores.mapNotNull { r -> r.opcionElegidaId?.let { r.preguntaId to it } }.toMap()
        val aciertos = anteriores.associate { it.preguntaId to it.esCorrecta }
        val primeraSinResponder = preguntas.indexOfFirst { it.pregunta.id !in respuestas }
        _uiState.update {
            it.copy(
                fase = Fase.Respondiendo,
                preguntas = preguntas,
                respuestas = respuestas,
                aciertos = aciertos,
                // Si respondió todo pero no alcanzó a terminar, queda en la última para presionar «Terminar»
                indiceActual = if (primeraSinResponder >= 0) primeraSinResponder else preguntas.lastIndex
            )
        }
    }

    /** El estudiante toca una alternativa: se guarda al instante y se bloquean las demás. */
    fun elegir(opcionId: Long) {
        val estado = _uiState.value
        val pregunta = estado.preguntaActual ?: return
        val intentoActual = intento ?: return
        if (estado.guardando || estado.opcionElegidaActual != null) return

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                val esCorrecta = resultadoRepository.responder(intentoActual.id, pregunta.pregunta.id, opcionId)
                _uiState.update {
                    it.copy(
                        guardando = false,
                        respuestas = it.respuestas + (pregunta.pregunta.id to opcionId),
                        aciertos = it.aciertos + (pregunta.pregunta.id to esCorrecta)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar tu respuesta. Intenta de nuevo.") }
            }
        }
    }

    /** Pasa a la siguiente pregunta; en la última cierra el intento y muestra el resultado. */
    fun siguiente() {
        val estado = _uiState.value
        if (estado.guardando || estado.opcionElegidaActual == null) return
        detenerAudio()

        if (!estado.esUltima) {
            _uiState.update { it.copy(indiceActual = it.indiceActual + 1) }
            return
        }

        val intentoActual = intento ?: return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                val resultado = resultadoRepository.finalizar(intentoActual)
                // Copia al servidor en segundo plano: sin esperar y sin mostrar errores al estudiante
                alcanceApp.launch { sincronizacionRepository.sincronizarPendientes() }
                val detalle = if (mostrarRespuestasAlTerminar) {
                    resultadoRepository.detalleDelIntento(intentoActual.id).first()
                } else {
                    emptyList()
                }
                _uiState.update {
                    it.copy(
                        guardando = false,
                        fase = Fase.Terminada(resultado.correctas, resultado.total),
                        mostrarDetalle = mostrarRespuestasAlTerminar,
                        detalle = detalle
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo terminar. Intenta de nuevo.") }
            }
        }
    }

    /** Escuchar (o detener) el enunciado de la pregunta en pantalla. */
    fun alternarAudioPregunta() {
        if (_uiState.value.reproduciendoAudio) {
            detenerAudio()
            return
        }
        val ruta = _uiState.value.preguntaActual?.pregunta?.recursoAudio ?: return
        _uiState.update { it.copy(reproduciendoAudio = true) }
        reproductor.reproducirArchivo(ruta) {
            _uiState.update { it.copy(reproduciendoAudio = false) }
        }
    }

    fun errorVisto() = _uiState.update { it.copy(error = null) }

    private fun detenerAudio() {
        reproductor.detenerAudio()
        _uiState.update { it.copy(reproduciendoAudio = false) }
    }

    private fun bloquear(mensaje: String) = _uiState.update { it.copy(fase = Fase.Bloqueada(mensaje)) }

    private fun fechaCorta(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATO_CORTO)

    override fun onCleared() {
        reproductor.detenerAudio()
        super.onCleared()
    }

    companion object {
        private const val MENSAJE_NO_DISPONIBLE = "Esta actividad no está disponible"
        private val FORMATO_CORTO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

        fun factory(actividadId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                RendirActividadViewModel(
                    contenidoRepository = app.container.contenidoRepository,
                    asignacionRepository = app.container.asignacionRepository,
                    resultadoRepository = app.container.resultadoRepository,
                    sincronizacionRepository = app.container.sincronizacionRepository,
                    sesion = app.container.sesion,
                    reproductor = AudioPlayerHelper(app),
                    alcanceApp = app.container.alcanceApp,
                    actividadId = actividadId
                )
            }
        }
    }
}
