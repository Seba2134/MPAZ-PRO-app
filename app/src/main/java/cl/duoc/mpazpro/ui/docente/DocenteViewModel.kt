package cl.duoc.mpazpro.ui.docente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.local.relation.PruebaDocente
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ResultadoSync
import cl.duoc.mpazpro.data.repository.SincronizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** El estado de la conexión con cada microservicio, para la tarjeta del inicio del docente. */
data class EstadoServidor(
    val usuarios: Boolean? = null,     // null = todavía revisando
    val resultados: Boolean? = null,
    val sincronizando: Boolean = false,
    val mensaje: String? = null
)

/** Inicio del docente: las pruebas de sus cursos y el envío de resultados al servidor. */
class DocenteViewModel(
    asignacionRepository: AsignacionRepository,
    private val sincronizacionRepository: SincronizacionRepository,
    docenteId: Long
) : ViewModel() {

    val pruebas: StateFlow<List<PruebaDocente>> =
        asignacionRepository.pruebasDelDocente(docenteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /** Resultados terminados en este dispositivo que aún no llegan al servidor. */
    val pendientes: StateFlow<Int> =
        sincronizacionRepository.pendientes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    private val _servidor = MutableStateFlow(EstadoServidor())
    val servidor: StateFlow<EstadoServidor> = _servidor.asStateFlow()

    init {
        revisarServidor()
    }

    private fun revisarServidor() {
        viewModelScope.launch {
            val estado = sincronizacionRepository.servidorDisponible()
            _servidor.update { it.copy(usuarios = estado.usuarios, resultados = estado.resultados) }
        }
    }

    /** Envía los pendientes (de cualquier estudiante: así se suben los de un equipo compartido). */
    fun sincronizarAhora() {
        if (_servidor.value.sincronizando) return
        _servidor.update { it.copy(sincronizando = true, mensaje = null) }
        viewModelScope.launch {
            val mensaje = when (val resultado = sincronizacionRepository.sincronizarPendientes()) {
                is ResultadoSync.Enviados -> "Se enviaron ${resultado.cantidad} resultados"
                ResultadoSync.SinConexion -> "No se pudo conectar"
                ResultadoSync.SinSesion -> "Vuelve a iniciar sesión con el servidor encendido"
            }
            val estado = sincronizacionRepository.servidorDisponible()
            _servidor.update {
                it.copy(
                    sincronizando = false,
                    usuarios = estado.usuarios,
                    resultados = estado.resultados,
                    mensaje = mensaje
                )
            }
        }
    }

    fun mensajeVisto() = _servidor.update { it.copy(mensaje = null) }

    companion object {
        fun factory(docenteId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                DocenteViewModel(
                    asignacionRepository = app.container.asignacionRepository,
                    sincronizacionRepository = app.container.sincronizacionRepository,
                    docenteId = docenteId
                )
            }
        }
    }
}
