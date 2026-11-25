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
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UnidadesUiState(
    val asignaturas: List<AsignaturaEntity> = emptyList(),
    val asignaturaId: Long? = null,
    val unidades: List<UnidadEntity> = emptyList(),
    // Formulario «Nueva unidad»
    val titulo: String = "",
    val errorTitulo: String? = null,
    val intentoEnviar: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null
)

/** Jefatura UTP: las unidades de cada asignatura y el formulario para crear una nueva. */
@OptIn(ExperimentalCoroutinesApi::class)
class UnidadesViewModel(
    private val contenidoRepository: ContenidoRepository,
    private val sesion: Sesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnidadesUiState())
    val uiState: StateFlow<UnidadesUiState> = _uiState.asStateFlow()

    private val asignaturaElegida = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            contenidoRepository.asignaturas().collect { asignaturas ->
                _uiState.update { it.copy(asignaturas = asignaturas) }
                if (asignaturaElegida.value == null) asignaturas.firstOrNull()?.let { elegirAsignatura(it.id) }
            }
        }
        viewModelScope.launch {
            asignaturaElegida.filterNotNull()
                .flatMapLatest { contenidoRepository.unidades(it) }
                .collect { unidades -> _uiState.update { it.copy(unidades = unidades) } }
        }
    }

    fun elegirAsignatura(asignaturaId: Long) {
        asignaturaElegida.value = asignaturaId
        _uiState.update { it.copy(asignaturaId = asignaturaId) }
    }

    fun onTituloChange(valor: String) = _uiState.update {
        it.copy(titulo = valor, errorTitulo = if (it.intentoEnviar) Validaciones.tituloUnidad(valor) else null)
    }

    fun crearUnidad() {
        val estado = _uiState.value
        if (estado.guardando) return
        val asignaturaId = estado.asignaturaId ?: return
        val error = Validaciones.tituloUnidad(estado.titulo)
        _uiState.update { it.copy(intentoEnviar = true, errorTitulo = error, mensaje = null) }
        if (error != null) return

        val utpId = sesion.usuarioActual.value?.id ?: return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val orden = contenidoRepository.siguienteOrdenUnidad(asignaturaId)
            val id = contenidoRepository.crearUnidad(asignaturaId, estado.titulo.trim(), orden, utpId)
            _uiState.update {
                if (id == ContenidoRepository.NO_CREADO) {
                    it.copy(guardando = false, mensaje = "Solo Jefatura UTP puede crear unidades")
                } else {
                    it.copy(
                        guardando = false,
                        titulo = "",
                        intentoEnviar = false,
                        errorTitulo = null,
                        mensaje = "Unidad creada en Borrador"
                    )
                }
            }
        }
    }

    fun mensajeVisto() = _uiState.update { it.copy(mensaje = null) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                UnidadesViewModel(app.container.contenidoRepository, app.container.sesion)
            }
        }
    }
}
