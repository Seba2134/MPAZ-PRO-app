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
import cl.duoc.mpazpro.data.remote.dto.ObservacionDto
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ContenidoRepository
import cl.duoc.mpazpro.data.repository.ObservacionRemotaRepository
import cl.duoc.mpazpro.data.repository.ResultadoRemoto
import cl.duoc.mpazpro.data.repository.ResultadoRepository
import cl.duoc.mpazpro.data.repository.UsuarioRepository
import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Qué pasó con la copia de la observación en el servidor. */
enum class EstadoEnvio { NINGUNO, ENVIANDO, GUARDADA, SIN_CONEXION, SIN_SESION, ERROR }

/** Lo que el servidor tiene de este estudiante en esta unidad (tarjeta «En el servidor»). */
data class DatosServidor(
    val cargando: Boolean = false,
    val consultado: Boolean = false,
    val problema: String? = null,             // «No se pudo conectar con el servidor», etc.
    val intentosRecibidos: Int = 0,
    val ultimoIntento: Long? = null,          // fechaFin del intento más reciente (epoch ms)
    val observacion: ObservacionDto? = null
)

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
    val mensaje: String? = null,
    // Servidor
    val envio: EstadoEnvio = EstadoEnvio.NINGUNO,
    val servidor: DatosServidor = DatosServidor()
) {
    val correctasTotales: Int get() = rendimiento.sumOf { it.correctas }
    val preguntasTotales: Int get() = rendimiento.sumOf { it.total }
    val porcentajeGlobal: Int get() = if (preguntasTotales == 0) 0 else correctasTotales * 100 / preguntasTotales

    /** En el dispositivo no hay observación y en el servidor sí: se puede traer. */
    val puedeUsarLaDelServidor: Boolean get() = observacionGuardada == null && servidor.observacion != null
}

/** El informe por OA de un estudiante: lo ve y lo valida el docente (nunca el estudiante). */
@OptIn(ExperimentalCoroutinesApi::class)
class InformeViewModel(
    private val asignacionRepository: AsignacionRepository,
    private val contenidoRepository: ContenidoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val resultadoRepository: ResultadoRepository,
    private val observacionRemota: ObservacionRemotaRepository,
    private val sesion: Sesion,
    private val asignacionId: Long,
    private val estudianteId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(InformeUiState())
    val uiState: StateFlow<InformeUiState> = _uiState.asStateFlow()

    // El texto se llena una sola vez con lo guardado, para no pisar lo que el docente escribe
    private var textoInicializado = false
    private var unidadId: Long = 0L

    // Cómo se identifica este informe en el servidor (no conoce los ids de Room)
    private var estudianteNombreUsuario: String = ""
    private var unidadTitulo: String = ""

    // Lo último que se intentó subir, para «Reintentar»
    private var ultimoEnvio: Pair<String, Boolean>? = null

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
            estudianteNombreUsuario = estudiante.nombreUsuario
            unidadTitulo = detalle?.unidadTitulo.orEmpty()
            _uiState.update {
                it.copy(
                    estudiante = estudiante.nombre,
                    asignatura = detalle?.asignaturaNombre.orEmpty(),
                    unidad = unidadTitulo
                )
            }

            // Lo del servidor se carga aparte: si no responde, el informe local se ve igual
            actualizarServidor()

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

    /** Guarda primero en Room (como siempre) y después sube la copia al servidor en segundo plano. */
    fun guardarObservacion() {
        val estado = _uiState.value
        if (estado.guardando) return
        val error = Validaciones.observacion(estado.textoObservacion)
        _uiState.update { it.copy(intentoEnviar = true, errorObservacion = error, mensaje = null) }
        if (error != null) return

        val docenteId = sesion.usuarioActual.value?.id ?: return
        val texto = estado.textoObservacion.trim()
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            if (!tieneAcceso()) return@launch
            resultadoRepository.guardarObservacion(estudianteId, unidadId, docenteId, texto)
            _uiState.update { it.copy(guardando = false, mensaje = "Observación guardada") }
            enviarAlServidor(texto, validado = false)
        }
    }

    /** Valida en Room y después sube la validación al servidor (mismo texto con validado = true). */
    fun validarInforme() {
        val estado = _uiState.value
        val guardada = estado.observacionGuardada
        if (estado.guardando || guardada == null) return
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            if (!tieneAcceso()) return@launch
            resultadoRepository.validarInforme(estudianteId, unidadId)
            _uiState.update { it.copy(guardando = false, mensaje = "Informe validado ✓") }
            enviarAlServidor(guardada.texto, validado = true)
        }
    }

    /** Vuelve a subir lo último que no se pudo enviar. */
    fun reintentarEnvio() {
        val (texto, validado) = ultimoEnvio ?: return
        viewModelScope.launch {
            if (!tieneAcceso()) return@launch
            enviarAlServidor(texto, validado)
        }
    }

    private suspend fun enviarAlServidor(texto: String, validado: Boolean) {
        ultimoEnvio = texto to validado
        _uiState.update { it.copy(envio = EstadoEnvio.ENVIANDO) }
        val resultado = observacionRemota.guardar(estudianteNombreUsuario, unidadTitulo, texto, validado)
        _uiState.update {
            when (resultado) {
                is ResultadoRemoto.Exito -> it.copy(
                    envio = EstadoEnvio.GUARDADA,
                    servidor = it.servidor.copy(observacion = resultado.dato, problema = null, consultado = true)
                )
                ResultadoRemoto.SinConexion -> it.copy(envio = EstadoEnvio.SIN_CONEXION)
                ResultadoRemoto.SinSesion -> it.copy(envio = EstadoEnvio.SIN_SESION)
                is ResultadoRemoto.Error -> it.copy(envio = EstadoEnvio.ERROR)
            }
        }
    }

    /** Recibir: trae los intentos (de esta unidad) y la observación que tiene el servidor. */
    fun actualizarServidor() {
        if (_uiState.value.servidor.cargando || estudianteNombreUsuario.isEmpty()) return
        _uiState.update { it.copy(servidor = it.servidor.copy(cargando = true)) }
        viewModelScope.launch {
            val observacion = async { observacionRemota.obtener(estudianteNombreUsuario, unidadTitulo) }
            val intentos = async { observacionRemota.intentosEnServidor(estudianteNombreUsuario) }
            val resultadoObservacion = observacion.await()
            val resultadoIntentos = intentos.await()

            val problema = problemaDe(resultadoObservacion) ?: problemaDe(resultadoIntentos)
            val deLaUnidad = (resultadoIntentos as? ResultadoRemoto.Exito)?.dato.orEmpty()
                .filter { it.unidadTitulo == unidadTitulo }
            _uiState.update {
                it.copy(
                    servidor = DatosServidor(
                        cargando = false,
                        consultado = true,
                        problema = problema,
                        intentosRecibidos = deLaUnidad.size,
                        ultimoIntento = deLaUnidad.maxOfOrNull { intento -> intento.fechaFin },
                        observacion = (resultadoObservacion as? ResultadoRemoto.Exito)?.dato
                    )
                )
            }
        }
    }

    /** Copia a este dispositivo la observación del servidor (y su validación, si venía validada). */
    fun usarLaDelServidor() {
        val estado = _uiState.value
        val remota = estado.servidor.observacion ?: return
        if (estado.guardando || estado.observacionGuardada != null) return
        val docenteId = sesion.usuarioActual.value?.id ?: return
        _uiState.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            if (!tieneAcceso()) return@launch
            resultadoRepository.guardarObservacion(estudianteId, unidadId, docenteId, remota.texto)
            if (remota.validado) resultadoRepository.validarInforme(estudianteId, unidadId)
            _uiState.update {
                it.copy(
                    guardando = false,
                    textoObservacion = remota.texto,
                    envio = EstadoEnvio.GUARDADA,
                    mensaje = "Observación copiada desde el servidor"
                )
            }
        }
    }

    fun mensajeVisto() = _uiState.update { it.copy(mensaje = null) }

    private fun problemaDe(resultado: ResultadoRemoto<*>): String? = when (resultado) {
        is ResultadoRemoto.Exito -> null
        ResultadoRemoto.SinConexion -> "No se pudo conectar con el servidor"
        ResultadoRemoto.SinSesion -> "Inicia sesión con el servidor encendido"
        is ResultadoRemoto.Error -> resultado.mensaje
    }

    companion object {
        fun factory(asignacionId: Long, estudianteId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                InformeViewModel(
                    asignacionRepository = app.container.asignacionRepository,
                    contenidoRepository = app.container.contenidoRepository,
                    usuarioRepository = app.container.usuarioRepository,
                    resultadoRepository = app.container.resultadoRepository,
                    observacionRemota = app.container.observacionRemotaRepository,
                    sesion = app.container.sesion,
                    asignacionId = asignacionId,
                    estudianteId = estudianteId
                )
            }
        }
    }
}
