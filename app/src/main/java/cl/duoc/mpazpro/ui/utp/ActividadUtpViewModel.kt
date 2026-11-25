package cl.duoc.mpazpro.ui.utp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.relation.PreguntaConOpciones
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.domain.model.TipoPregunta
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActividadUtpUiState(
    val cargando: Boolean = true,
    val actividad: ActividadEntity? = null,
    val enBorrador: Boolean = false,
    val objetivos: List<ObjetivoAprendizajeEntity> = emptyList(),
    val preguntas: List<PreguntaConOpciones> = emptyList(),
    // Formulario «Nueva pregunta»
    val oaId: Long? = null,
    val enunciado: String = "",
    val alternativas: List<String> = List(CANTIDAD_ALTERNATIVAS) { "" },
    val correcta: Int? = null,
    val errorOa: String? = null,
    val errorEnunciado: String? = null,
    val errorAlternativas: String? = null,
    val intentoEnviar: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null
) {
    /** El código de un OA de la unidad (para mostrarlo junto a cada pregunta). */
    fun codigoOa(oaId: Long): String = objetivos.firstOrNull { it.id == oaId }?.codigo.orEmpty()

    companion object {
        const val CANTIDAD_ALTERNATIVAS = 4
    }
}

/** Jefatura UTP: las preguntas de un desafío o prueba final, y el formulario para agregar más. */
class ActividadUtpViewModel(
    private val contenidoRepository: ContenidoRepository,
    private val sesion: Sesion,
    private val actividadId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActividadUtpUiState())
    val uiState: StateFlow<ActividadUtpUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val actividad = contenidoRepository.actividad(actividadId)
            if (actividad == null) {
                _uiState.update { it.copy(cargando = false) }
                return@launch
            }
            combine(
                contenidoRepository.observarUnidad(actividad.unidadId),
                contenidoRepository.objetivos(actividad.unidadId),
                contenidoRepository.preguntasConOpciones(actividadId)
            ) { unidad, objetivos, preguntas -> Triple(unidad, objetivos, preguntas) }
                .collect { (unidad, objetivos, preguntas) ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            actividad = actividad,
                            enBorrador = unidad?.estado == EstadoContenido.BORRADOR,
                            objetivos = objetivos,
                            preguntas = preguntas.map { p -> p.copy(opciones = p.opciones.sortedBy { o -> o.orden }) }
                        )
                    }
                }
        }
    }

    fun onOaChange(oaId: Long) = _uiState.update { revalidar(it.copy(oaId = oaId)) }

    fun onEnunciadoChange(valor: String) = _uiState.update { revalidar(it.copy(enunciado = valor)) }

    fun onAlternativaChange(indice: Int, valor: String) = _uiState.update {
        revalidar(it.copy(alternativas = it.alternativas.mapIndexed { i, actual -> if (i == indice) valor else actual }))
    }

    fun onCorrectaChange(indice: Int) = _uiState.update { revalidar(it.copy(correcta = indice)) }

    fun guardarPregunta() {
        val estado = _uiState.value
        if (estado.guardando) return
        val validado = conErrores(estado.copy(intentoEnviar = true, mensaje = null))
        _uiState.value = validado
        if (listOfNotNull(validado.errorOa, validado.errorEnunciado, validado.errorAlternativas).isNotEmpty()) return

        val utpId = sesion.usuarioActual.value?.id ?: return
        val oaId = estado.oaId ?: return
        val correcta = estado.correcta ?: return

        // Solo las alternativas escritas, con orden 1..n; la correcta conserva su marca
        val opciones = estado.alternativas
            .mapIndexed { indice, texto -> indice to texto.trim() }
            .filter { (_, texto) -> texto.isNotEmpty() }
            .mapIndexed { posicion, (indice, texto) ->
                OpcionEntity(preguntaId = 0, texto = texto, esCorrecta = indice == correcta, orden = posicion + 1)
            }

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val orden = contenidoRepository.cantidadPreguntas(actividadId) + 1
            val id = contenidoRepository.crearPreguntaConOpciones(
                pregunta = PreguntaEntity(
                    actividadId = actividadId,
                    oaId = oaId,
                    enunciado = estado.enunciado.trim(),
                    tipo = TipoPregunta.SELECCION,
                    orden = orden
                ),
                opciones = opciones,
                utpId = utpId
            )
            _uiState.update {
                if (id == ContenidoRepository.NO_CREADO) {
                    it.copy(guardando = false, mensaje = "No se pudo guardar. Para editar, vuelve la unidad a Borrador.")
                } else {
                    // Se limpia el formulario, pero se mantiene el OA elegido para la siguiente pregunta
                    it.copy(
                        guardando = false,
                        enunciado = "",
                        alternativas = List(ActividadUtpUiState.CANTIDAD_ALTERNATIVAS) { "" },
                        correcta = null,
                        intentoEnviar = false,
                        errorOa = null,
                        errorEnunciado = null,
                        errorAlternativas = null,
                        mensaje = "Pregunta guardada"
                    )
                }
            }
        }
    }

    fun mensajeVisto() = _uiState.update { it.copy(mensaje = null) }

    private fun revalidar(estado: ActividadUtpUiState): ActividadUtpUiState =
        if (estado.intentoEnviar) conErrores(estado) else estado

    private fun conErrores(estado: ActividadUtpUiState): ActividadUtpUiState = estado.copy(
        errorOa = Validaciones.objetivoSeleccionado(estado.oaId),
        errorEnunciado = Validaciones.enunciadoPregunta(estado.enunciado),
        errorAlternativas = Validaciones.alternativasPregunta(estado.alternativas, estado.correcta)
    )

    companion object {
        fun factory(actividadId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                ActividadUtpViewModel(app.container.contenidoRepository, app.container.sesion, actividadId)
            }
        }
    }
}
