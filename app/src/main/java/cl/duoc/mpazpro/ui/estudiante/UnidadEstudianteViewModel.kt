package cl.duoc.mpazpro.ui.estudiante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.AsignacionEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.IntentoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.data.repository.ResultadoRepository
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.util.AudioPlayerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Un objetivo de aprendizaje con sus contenidos, en orden. */
data class OaConContenidos(
    val objetivo: ObjetivoAprendizajeEntity,
    val contenidos: List<ContenidoEntity>
)

/** Si el estudiante puede rendir la prueba final ahora y, si no puede, por qué. */
data class EstadoPruebaFinal(
    val puedeRendir: Boolean,
    val textoBoton: String,
    val motivo: String? = null
)

data class UnidadEstudianteUiState(
    val cargando: Boolean = true,
    val sinAcceso: Boolean = false,
    val titulo: String = "",
    val objetivos: List<OaConContenidos> = emptyList(),
    val desafios: List<ActividadEntity> = emptyList(),
    val pruebaFinal: ActividadEntity? = null,
    val asignacion: AsignacionEntity? = null,
    val intentosPrueba: List<IntentoEntity> = emptyList(),
    val terminadas: Set<Long> = emptySet(),
    val reproduciendoId: Long? = null,
    val avance: Int = 0,
    val estadoPrueba: EstadoPruebaFinal? = null
)

/** Una unidad vista por el estudiante: lo que aprende, sus desafíos y su prueba final. */
class UnidadEstudianteViewModel(
    private val contenidoRepository: ContenidoRepository,
    private val asignacionRepository: AsignacionRepository,
    private val resultadoRepository: ResultadoRepository,
    private val sesion: Sesion,
    private val reproductor: AudioPlayerHelper,
    private val unidadId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnidadEstudianteUiState())
    val uiState: StateFlow<UnidadEstudianteUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val estudiante = sesion.usuarioActual.value
            val cursoId = estudiante?.cursoId
            val unidad = contenidoRepository.unidad(unidadId)
            val asignacion = cursoId?.let { asignacionRepository.asignacion(it, unidadId) }

            // Solo se abre si está publicada y asignada a SU curso
            if (estudiante == null || unidad == null || asignacion == null ||
                unidad.estado != EstadoContenido.PUBLICADO
            ) {
                _uiState.value = UnidadEstudianteUiState(cargando = false, sinAcceso = true)
                return@launch
            }

            val prueba = contenidoRepository.pruebaFinal(unidadId)
            val intentosPrueba = prueba?.let { resultadoRepository.intentosDelEstudiante(estudiante.id, it.id) }
                ?: flowOf(emptyList())

            combine(
                contenidoRepository.objetivos(unidadId),
                contenidoRepository.contenidosDeUnidad(unidadId),
                contenidoRepository.desafios(unidadId),
                resultadoRepository.actividadesTerminadas(estudiante.id),
                intentosPrueba
            ) { objetivos, contenidos, desafios, terminadas, intentos ->
                val porOa = contenidos.groupBy { it.oaId }
                val terminadasSet = terminadas.toSet()
                _uiState.value.copy(
                    cargando = false,
                    titulo = unidad.titulo,
                    objetivos = objetivos.map { OaConContenidos(it, porOa[it.id].orEmpty()) },
                    desafios = desafios,
                    pruebaFinal = prueba,
                    asignacion = asignacion,
                    intentosPrueba = intentos,
                    terminadas = terminadasSet,
                    avance = calcularAvance(desafios, prueba, terminadasSet),
                    estadoPrueba = prueba?.let { estadoPruebaFinal(asignacion, intentos) }
                )
            }.collect { nuevo ->
                // reproduciendoId puede haber cambiado mientras tanto: se conserva el actual
                _uiState.update { nuevo.copy(reproduciendoId = it.reproduciendoId) }
            }
        }
    }

    /** Escuchar o detener un contenido de audio. Solo suena uno a la vez. */
    fun alternarAudio(contenido: ContenidoEntity) {
        val ruta = contenido.recurso ?: return
        if (_uiState.value.reproduciendoId == contenido.id) {
            reproductor.detenerAudio()
            _uiState.update { it.copy(reproduciendoId = null) }
            return
        }
        _uiState.update { it.copy(reproduciendoId = contenido.id) }
        reproductor.reproducirArchivo(ruta) {
            _uiState.update { if (it.reproduciendoId == contenido.id) it.copy(reproduciendoId = null) else it }
        }
    }

    override fun onCleared() {
        reproductor.detenerAudio()
        super.onCleared()
    }

    /** Porcentaje de actividades (desafíos + prueba final) que ya terminó al menos una vez. */
    private fun calcularAvance(
        desafios: List<ActividadEntity>,
        prueba: ActividadEntity?,
        terminadas: Set<Long>
    ): Int {
        val actividades = desafios.map { it.id } + listOfNotNull(prueba?.id)
        if (actividades.isEmpty()) return 0
        return actividades.count { it in terminadas } * 100 / actividades.size
    }

    /** Las mismas reglas que aplica ResultadoRepository.iniciarActividad, explicadas en palabras simples. */
    private fun estadoPruebaFinal(
        asignacion: AsignacionEntity,
        intentos: List<IntentoEntity>,
        ahora: Long = System.currentTimeMillis()
    ): EstadoPruebaFinal {
        if (intentos.any { it.fechaFin == null }) {
            return EstadoPruebaFinal(puedeRendir = true, textoBoton = "Continuar prueba")
        }
        val inicio = asignacion.fechaInicioPrueba
        val fin = asignacion.fechaFinPrueba
        return when {
            inicio != null && ahora < inicio ->
                EstadoPruebaFinal(false, "Rendir prueba", "La prueba abre el ${fechaCorta(inicio)}")
            fin != null && ahora > fin ->
                EstadoPruebaFinal(false, "Rendir prueba", "La prueba ya cerró")
            intentos.size >= asignacion.intentosMaxPrueba ->
                EstadoPruebaFinal(false, "Rendir prueba", "Ya usaste tus ${asignacion.intentosMaxPrueba} intentos")
            else -> EstadoPruebaFinal(puedeRendir = true, textoBoton = "Rendir prueba")
        }
    }

    private fun fechaCorta(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATO_CORTO)

    companion object {
        private val FORMATO_CORTO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

        fun factory(unidadId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                UnidadEstudianteViewModel(
                    contenidoRepository = app.container.contenidoRepository,
                    asignacionRepository = app.container.asignacionRepository,
                    resultadoRepository = app.container.resultadoRepository,
                    sesion = app.container.sesion,
                    reproductor = AudioPlayerHelper(app),
                    unidadId = unidadId
                )
            }
        }
    }
}
