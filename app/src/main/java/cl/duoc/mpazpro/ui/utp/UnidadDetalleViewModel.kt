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
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.data.local.relation.ActividadResumen
import cl.duoc.mpazpro.data.local.relation.HistorialConUsuario
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.domain.model.EstadoContenido
import cl.duoc.mpazpro.domain.model.TipoActividad
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UnidadDetalleUiState(
    val cargando: Boolean = true,
    val unidad: UnidadEntity? = null,
    val objetivos: List<ObjetivoAprendizajeEntity> = emptyList(),
    val actividades: List<ActividadResumen> = emptyList(),
    val historial: List<HistorialConUsuario> = emptyList(),
    // Formulario «Agregar OA»
    val codigoOa: String = "",
    val descripcionOa: String = "",
    val errorCodigoOa: String? = null,
    val errorDescripcionOa: String? = null,
    val intentoEnviarOa: Boolean = false,
    // Formulario «Nueva actividad»
    val tituloActividad: String = "",
    val instruccionActividad: String = "",
    val tipoActividad: TipoActividad = TipoActividad.DESAFIO,
    val errorTituloActividad: String? = null,
    val errorInstruccionActividad: String? = null,
    val intentoEnviarActividad: Boolean = false,
    // Cambiar estado
    val comentario: String = "",
    val errorComentario: String? = null,
    val estadoPorConfirmar: EstadoContenido? = null,   // Publicar y Deshabilitar piden confirmación
    val guardando: Boolean = false,
    val mensaje: String? = null
) {
    val enBorrador: Boolean get() = unidad?.estado == EstadoContenido.BORRADOR
    val tienePruebaFinal: Boolean get() = actividades.any { it.tipo == TipoActividad.PRUEBA_FINAL }
}

/** Detalle de una unidad para Jefatura UTP: OA, actividades, cambio de estado e historial. */
class UnidadDetalleViewModel(
    private val contenidoRepository: ContenidoRepository,
    private val sesion: Sesion,
    private val unidadId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnidadDetalleUiState())
    val uiState: StateFlow<UnidadDetalleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                contenidoRepository.observarUnidad(unidadId),
                contenidoRepository.objetivos(unidadId),
                contenidoRepository.resumenActividades(unidadId),
                contenidoRepository.historialConUsuario(unidadId)
            ) { unidad, objetivos, actividades, historial ->
                _uiState.value.copy(
                    cargando = false,
                    unidad = unidad,
                    objetivos = objetivos,
                    actividades = actividades,
                    historial = historial
                )
            }.collect { nuevo ->
                // Se conservan los formularios tal como están; solo se actualizan los datos
                _uiState.update {
                    it.copy(
                        cargando = false,
                        unidad = nuevo.unidad,
                        objetivos = nuevo.objetivos,
                        actividades = nuevo.actividades,
                        historial = nuevo.historial,
                        tipoActividad = if (nuevo.tienePruebaFinal) TipoActividad.DESAFIO else it.tipoActividad
                    )
                }
            }
        }
    }

    // ---------- Agregar OA ----------

    fun onCodigoOaChange(valor: String) = _uiState.update {
        it.copy(codigoOa = valor, errorCodigoOa = if (it.intentoEnviarOa) Validaciones.codigoOa(valor) else null)
    }

    fun onDescripcionOaChange(valor: String) = _uiState.update {
        it.copy(
            descripcionOa = valor,
            errorDescripcionOa = if (it.intentoEnviarOa) Validaciones.descripcionOa(valor) else null
        )
    }

    fun agregarOa() {
        val estado = _uiState.value
        if (estado.guardando) return
        val validado = estado.copy(
            intentoEnviarOa = true,
            errorCodigoOa = Validaciones.codigoOa(estado.codigoOa),
            errorDescripcionOa = Validaciones.descripcionOa(estado.descripcionOa),
            mensaje = null
        )
        _uiState.value = validado
        if (validado.errorCodigoOa != null || validado.errorDescripcionOa != null) return

        val utpId = sesion.usuarioActual.value?.id ?: return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val id = contenidoRepository.crearObjetivo(
                ObjetivoAprendizajeEntity(
                    unidadId = unidadId,
                    codigo = estado.codigoOa.trim(),
                    descripcion = estado.descripcionOa.trim()
                ),
                utpId
            )
            _uiState.update {
                if (id == ContenidoRepository.NO_CREADO) {
                    it.copy(guardando = false, mensaje = MENSAJE_NO_EDITABLE)
                } else {
                    it.copy(guardando = false, codigoOa = "", descripcionOa = "", intentoEnviarOa = false, mensaje = "OA agregado")
                }
            }
        }
    }

    // ---------- Nueva actividad ----------

    fun onTituloActividadChange(valor: String) = _uiState.update {
        it.copy(
            tituloActividad = valor,
            errorTituloActividad = if (it.intentoEnviarActividad) Validaciones.tituloActividad(valor) else null
        )
    }

    fun onInstruccionActividadChange(valor: String) = _uiState.update {
        it.copy(
            instruccionActividad = valor,
            errorInstruccionActividad = if (it.intentoEnviarActividad) Validaciones.instruccionActividad(valor) else null
        )
    }

    fun onTipoActividadChange(tipo: TipoActividad) = _uiState.update {
        if (tipo == TipoActividad.PRUEBA_FINAL && it.tienePruebaFinal) it else it.copy(tipoActividad = tipo)
    }

    fun crearActividad() {
        val estado = _uiState.value
        if (estado.guardando) return
        val validado = estado.copy(
            intentoEnviarActividad = true,
            errorTituloActividad = Validaciones.tituloActividad(estado.tituloActividad),
            errorInstruccionActividad = Validaciones.instruccionActividad(estado.instruccionActividad),
            mensaje = null
        )
        _uiState.value = validado
        if (validado.errorTituloActividad != null || validado.errorInstruccionActividad != null) return

        val utpId = sesion.usuarioActual.value?.id ?: return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val orden = contenidoRepository.siguienteOrdenActividad(unidadId)
            val id = contenidoRepository.crearActividad(
                ActividadEntity(
                    unidadId = unidadId,
                    titulo = estado.tituloActividad.trim(),
                    instruccion = estado.instruccionActividad.trim(),
                    tipo = estado.tipoActividad,
                    orden = orden
                ),
                utpId
            )
            _uiState.update {
                if (id == ContenidoRepository.NO_CREADO) {
                    it.copy(guardando = false, mensaje = "No se pudo crear la actividad. Revisa que la unidad esté en Borrador y que no tenga ya una prueba final.")
                } else {
                    it.copy(
                        guardando = false,
                        tituloActividad = "",
                        instruccionActividad = "",
                        tipoActividad = TipoActividad.DESAFIO,
                        intentoEnviarActividad = false,
                        mensaje = "Actividad creada. Tócala para agregarle preguntas."
                    )
                }
            }
        }
    }

    // ---------- Cambiar estado ----------

    fun onComentarioChange(valor: String) = _uiState.update {
        it.copy(comentario = valor, errorComentario = Validaciones.comentarioCambio(valor))
    }

    /** Publicar y Deshabilitar piden confirmación; los demás cambios se hacen al tiro. */
    fun solicitarCambio(nuevo: EstadoContenido) {
        if (nuevo == EstadoContenido.PUBLICADO || nuevo == EstadoContenido.DESHABILITADO) {
            _uiState.update { it.copy(estadoPorConfirmar = nuevo) }
        } else {
            cambiarEstado(nuevo)
        }
    }

    fun confirmarCambio() {
        val nuevo = _uiState.value.estadoPorConfirmar ?: return
        _uiState.update { it.copy(estadoPorConfirmar = null) }
        cambiarEstado(nuevo)
    }

    fun cancelarCambio() = _uiState.update { it.copy(estadoPorConfirmar = null) }

    private fun cambiarEstado(nuevo: EstadoContenido) {
        val estado = _uiState.value
        if (estado.guardando) return
        val errorComentario = Validaciones.comentarioCambio(estado.comentario)
        if (errorComentario != null) {
            _uiState.update { it.copy(errorComentario = errorComentario) }
            return
        }
        val utpId = sesion.usuarioActual.value?.id ?: return
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            val ok = contenidoRepository.cambiarEstadoUnidad(
                unidadId = unidadId,
                nuevo = nuevo,
                usuarioId = utpId,
                comentario = estado.comentario.trim().ifBlank { null }
            )
            _uiState.update {
                if (ok) {
                    it.copy(guardando = false, comentario = "", mensaje = "La unidad quedó en «${nuevo.nombreVisible}»")
                } else {
                    it.copy(guardando = false, mensaje = "No se pudo cambiar el estado")
                }
            }
        }
    }

    fun mensajeVisto() = _uiState.update { it.copy(mensaje = null) }

    companion object {
        private const val MENSAJE_NO_EDITABLE = "Para editar, vuelve la unidad a Borrador"

        fun factory(unidadId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                UnidadDetalleViewModel(app.container.contenidoRepository, app.container.sesion, unidadId)
            }
        }
    }
}
