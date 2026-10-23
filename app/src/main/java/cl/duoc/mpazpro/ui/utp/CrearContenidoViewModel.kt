package cl.duoc.mpazpro.ui.utp

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.relation.OaConUnidad
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.domain.model.Licencia
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.model.TipoContenido
import cl.duoc.mpazpro.domain.validacion.Validaciones
import cl.duoc.mpazpro.util.ArchivosContenido
import cl.duoc.mpazpro.util.AudioPlayerHelper
import cl.duoc.mpazpro.util.GrabadorAudio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class CrearContenidoUiState(
    val asignaturas: List<AsignaturaEntity> = emptyList(),
    val objetivos: List<OaConUnidad> = emptyList(),
    val asignaturaId: Long? = null,
    val oaId: Long? = null,
    val tipo: TipoContenido = TipoContenido.TEXTO,
    val texto: String = "",
    val orden: String = "1",
    val autor: String = "",
    val licencia: Licencia = Licencia.PROPIA,
    val rutaFoto: String? = null,
    val rutaAudio: String? = null,
    val grabando: Boolean = false,
    val reproduciendo: Boolean = false,
    val errorOa: String? = null,
    val errorTexto: String? = null,
    val errorRecurso: String? = null,
    val errorOrden: String? = null,
    val errorAutor: String? = null,
    val errorGeneral: String? = null,
    val intentoEnviar: Boolean = false,
    val guardando: Boolean = false,
    val guardadoOk: Boolean = false
) {
    val objetivosDeLaAsignatura: List<OaConUnidad>
        get() = objetivos.filter { it.asignaturaId == asignaturaId }
}

class CrearContenidoViewModel(
    private val contenidoRepository: ContenidoRepository,
    private val sesion: Sesion,
    private val archivos: ArchivosContenido,
    private val grabador: GrabadorAudio,
    private val reproductor: AudioPlayerHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearContenidoUiState())
    val uiState: StateFlow<CrearContenidoUiState> = _uiState.asStateFlow()

    private var fotoPendiente: File? = null
    private var guardado = false

    init {
        viewModelScope.launch {
            contenidoRepository.asignaturas().collect { lista ->
                _uiState.update { it.copy(asignaturas = lista) }
            }
        }
        viewModelScope.launch {
            contenidoRepository.objetivosConUnidad().collect { lista ->
                _uiState.update { it.copy(objetivos = lista) }
            }
        }
    }

    fun onAsignaturaChange(asignaturaId: Long) {
        _uiState.update { revalidar(it.copy(asignaturaId = asignaturaId, oaId = null)) }
    }

    fun onOaChange(oaId: Long) {
        _uiState.update { revalidar(it.copy(oaId = oaId)) }
        viewModelScope.launch {
            val siguiente = contenidoRepository.siguienteOrden(oaId)
            _uiState.update { revalidar(it.copy(orden = siguiente.toString())) }
        }
    }

    fun onTipoChange(tipo: TipoContenido) {
        if (_uiState.value.grabando) detenerGrabacion()
        detenerReproduccion()
        _uiState.update { revalidar(it.copy(tipo = tipo)) }
    }

    fun onTextoChange(valor: String) = _uiState.update { revalidar(it.copy(texto = valor)) }
    fun onOrdenChange(valor: String) = _uiState.update { revalidar(it.copy(orden = valor)) }
    fun onAutorChange(valor: String) = _uiState.update { revalidar(it.copy(autor = valor)) }
    fun onLicenciaChange(licencia: Licencia) = _uiState.update { it.copy(licencia = licencia) }

    fun prepararFoto(): Uri {
        fotoPendiente?.delete()
        val archivo = archivos.nuevaFoto()
        fotoPendiente = archivo
        return archivos.uriPara(archivo)
    }

    fun onFotoTomada(exito: Boolean) {
        val archivo = fotoPendiente
        fotoPendiente = null
        if (exito && archivo != null && archivo.length() > 0) {
            archivos.borrar(_uiState.value.rutaFoto)
            _uiState.update {
                revalidar(
                    it.copy(
                        rutaFoto = archivo.absolutePath,
                        errorGeneral = null
                    )
                )
            }
        } else {
            archivo?.delete()
        }
    }

    fun iniciarGrabacion() {
        detenerReproduccion()
        val archivo = archivos.nuevoAudio()
        try {
            grabador.iniciar(archivo)
            _uiState.update { it.copy(grabando = true, errorGeneral = null) }
        } catch (e: Exception) {
            archivo.delete()
            _uiState.update {
                it.copy(
                    grabando = false,
                    errorGeneral = "No se pudo usar el micrófono. Intenta de nuevo."
                )
            }
        }
    }

    fun detenerGrabacion() {
        val archivo = grabador.detener()
        if (archivo != null && archivo.length() > 0) {
            archivos.borrar(_uiState.value.rutaAudio)
            _uiState.update {
                revalidar(
                    it.copy(
                        grabando = false,
                        rutaAudio = archivo.absolutePath
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    grabando = false,
                    errorRecurso = "La grabación fue muy corta. Graba de nuevo."
                )
            }
        }
    }

    fun alternarReproduccion() {
        val estado = uiState.value
        if (estado.reproduciendo) {
            detenerReproduccion()
            return
        }
        val ruta = estado.rutaAudio ?: return
        _uiState.update { it.copy(reproduciendo = true) }
        reproductor.reproducirArchivo(ruta) {
            _uiState.update { it.copy(reproduciendo = false) }
        }
    }

    private fun detenerReproduccion() {
        reproductor.detenerAudio()
        _uiState.update { it.copy(reproduciendo = false) }
    }

    fun guardar() {
        val estado = uiState.value
        if (estado.guardando) return
        if (estado.grabando) detenerGrabacion()

        val validado = conErrores(_uiState.value.copy(intentoEnviar = true, errorGeneral = null))
        val hayErrores = listOfNotNull(
            validado.errorOa, validado.errorTexto, validado.errorRecurso,
            validado.errorOrden, validado.errorAutor
        ).isNotEmpty()

        if (hayErrores) {
            _uiState.value = validado
            return
        }

        if (sesion.usuarioActual.value?.rol != Rol.JEFATURA_UTP) {
            _uiState.value = validado.copy(errorGeneral = "Solo Jefatura UTP puede crear contenido")
            return
        }

        _uiState.value = validado.copy(guardando = true)

        viewModelScope.launch {
            val recurso = when (validado.tipo) {
                TipoContenido.IMAGEN -> validado.rutaFoto
                TipoContenido.AUDIO -> validado.rutaAudio
                else -> null
            }
            try {
                contenidoRepository.crearContenido(
                    ContenidoEntity(
                        oaId = validado.oaId!!,
                        tipo = validado.tipo,
                        texto = validado.texto.trim().ifBlank { null },
                        recurso = recurso,
                        orden = validado.orden.trim().toInt(),
                        autor = validado.autor.trim(),
                        licencia = validado.licencia
                    )
                )
                if (validado.tipo != TipoContenido.IMAGEN) archivos.borrar(validado.rutaFoto)
                if (validado.tipo != TipoContenido.AUDIO) archivos.borrar(validado.rutaAudio)
                guardado = true
                _uiState.update { it.copy(guardando = false, guardadoOk = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "No se pudo guardar el contenido. Intenta de nuevo."
                    )
                }
            }
        }
    }

    private fun revalidar(estado: CrearContenidoUiState): CrearContenidoUiState =
        if (estado.intentoEnviar) conErrores(estado) else estado

    private fun conErrores(estado: CrearContenidoUiState): CrearContenidoUiState = estado.copy(
        errorOa = Validaciones.objetivoSeleccionado(estado.oaId),
        errorTexto = Validaciones.textoContenido(estado.tipo, estado.texto),
        errorRecurso = Validaciones.recursoContenido(
            estado.tipo,
            estado.rutaFoto,
            estado.rutaAudio
        ),
        errorOrden = Validaciones.orden(estado.orden),
        errorAutor = Validaciones.autor(estado.autor)
    )

    override fun onCleared() {
        grabador.cancelar()
        reproductor.detenerAudio()
        fotoPendiente?.delete()
        if (!guardado) {
            archivos.borrar(_uiState.value.rutaFoto)
            archivos.borrar(_uiState.value.rutaAudio)
        }
        super.onCleared()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                CrearContenidoViewModel(
                    contenidoRepository = app.container.contenidoRepository,
                    sesion = app.container.sesion,
                    archivos = ArchivosContenido(app),
                    grabador = GrabadorAudio(app),
                    reproductor = AudioPlayerHelper(app)
                )
            }
        }
    }
}