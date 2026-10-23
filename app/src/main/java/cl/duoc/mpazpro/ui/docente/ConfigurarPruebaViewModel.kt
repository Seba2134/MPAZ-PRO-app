package cl.duoc.mpazpro.ui.docente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazpro.MpazApp
import cl.duoc.mpazpro.data.Sesion
import cl.duoc.mpazpro.data.repository.AsignacionRepository
import cl.duoc.mpazpro.data.repository.ResultadoConfiguracion
import cl.duoc.mpazpro.domain.validacion.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Todo lo que el formulario "Configurar prueba" necesita para dibujarse. */
data class ConfigurarPruebaUiState(
    // Encabezado
    val cargandoDatos: Boolean = true,
    val unidad: String = "",
    val asignatura: String = "",
    val curso: String = "",

    // Campos del formulario
    val instrucciones: String = "",              // String
    val intentos: String = "",                   // Int (se escribe como texto)
    val fechaApertura: LocalDate? = null,        // Date
    val fechaAperturaGuardada: LocalDate? = null,
    val sinFechaLimite: Boolean = true,          // Switch
    val fechaCierre: LocalDate? = null,          // Date
    val mostrarRespuestas: Boolean = true,       // Checkbox

    // Errores por campo
    val errorInstrucciones: String? = null,
    val errorIntentos: String? = null,
    val errorFechaApertura: String? = null,
    val errorFechaCierre: String? = null,
    val errorGeneral: String? = null,

    // Estado del envío
    val intentoEnviar: Boolean = false,
    val guardando: Boolean = false,
    val guardadoOk: Boolean = false
)

class ConfigurarPruebaViewModel(
    private val asignacionRepository: AsignacionRepository,
    private val sesion: Sesion,
    private val asignacionId: Long
) : ViewModel() {

    private val zona: ZoneId = ZoneId.systemDefault()

    private val _uiState = MutableStateFlow(ConfigurarPruebaUiState())
    val uiState: StateFlow<ConfigurarPruebaUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    /** Lee la configuración actual de la base de datos y llena el formulario. */
    private fun cargar() {
        viewModelScope.launch {
            val detalle = asignacionRepository.detallePrueba(asignacionId)
            val asignacion = asignacionRepository.asignacionPorId(asignacionId)

            if (detalle == null || asignacion == null) {
                _uiState.update { it.copy(cargandoDatos = false, errorGeneral = "No se encontró esta prueba.") }
                return@launch
            }

            val apertura = asignacion.fechaInicioPrueba?.let { aFecha(it) }
            val cierre = asignacion.fechaFinPrueba?.let { aFecha(it) }

            _uiState.update {
                it.copy(
                    cargandoDatos = false,
                    unidad = detalle.unidadTitulo,
                    asignatura = detalle.asignaturaNombre,
                    curso = detalle.cursoNombre,
                    instrucciones = asignacion.instrucciones,
                    intentos = asignacion.intentosMaxPrueba.toString(),
                    fechaApertura = apertura ?: LocalDate.now(zona),
                    fechaAperturaGuardada = apertura,
                    sinFechaLimite = cierre == null,
                    fechaCierre = cierre,
                    mostrarRespuestas = asignacion.mostrarRespuestas
                )
            }
        }
    }

    // ---------- Eventos que llegan desde la pantalla ----------

    fun onInstruccionesChange(valor: String) = _uiState.update { revalidar(it.copy(instrucciones = valor)) }

    fun onIntentosChange(valor: String) = _uiState.update { revalidar(it.copy(intentos = valor)) }

    fun onFechaAperturaChange(fecha: LocalDate) = _uiState.update { revalidar(it.copy(fechaApertura = fecha)) }

    fun onFechaCierreChange(fecha: LocalDate) = _uiState.update { revalidar(it.copy(fechaCierre = fecha)) }

    fun onSinFechaLimiteChange(activo: Boolean) = _uiState.update { revalidar(it.copy(sinFechaLimite = activo)) }

    fun onMostrarRespuestasChange(activo: Boolean) = _uiState.update { it.copy(mostrarRespuestas = activo) }

    // ---------- Guardar ----------

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando || estado.cargandoDatos) return

        // 1) Validar todos los campos
        val validado = conErrores(estado.copy(intentoEnviar = true, errorGeneral = null))
        val hayErrores = listOfNotNull(
            validado.errorInstrucciones,
            validado.errorIntentos,
            validado.errorFechaApertura,
            validado.errorFechaCierre
        ).isNotEmpty()

        if (hayErrores) {
            _uiState.value = validado
            return
        }

        // 2) Quién está guardando
        val docenteId = sesion.usuarioActual.value?.id ?: run {
            _uiState.value = validado.copy(errorGeneral = "Tu sesión terminó. Vuelve a iniciar sesión.")
            return
        }

        _uiState.value = validado.copy(guardando = true)

        // 3) Convertir las fechas: apertura desde las 00:00, cierre hasta las 23:59 de ese día
        val inicioMillis = estado.fechaApertura?.atStartOfDay(zona)?.toInstant()?.toEpochMilli()
        val finMillis = if (estado.sinFechaLimite) {
            null
        } else {
            estado.fechaCierre?.atTime(LocalTime.MAX)?.atZone(zona)?.toInstant()?.toEpochMilli()
        }

        // 4) Guardar
        viewModelScope.launch {
            val resultado = asignacionRepository.configurarPrueba(
                docenteId = docenteId,
                asignacionId = asignacionId,
                instrucciones = estado.instrucciones.trim(),
                intentosMax = estado.intentos.trim().toInt(),
                fechaInicio = inicioMillis,
                fechaFin = finMillis,
                mostrarRespuestas = estado.mostrarRespuestas
            )

            _uiState.update {
                when (resultado) {
                    ResultadoConfiguracion.GUARDADA ->
                        it.copy(guardando = false, guardadoOk = true)
                    ResultadoConfiguracion.DATOS_INVALIDOS ->
                        it.copy(guardando = false, errorGeneral = "Revisa los datos ingresados")
                    ResultadoConfiguracion.SIN_ACCESO ->
                        it.copy(guardando = false, errorGeneral = "No tienes acceso a esta asignatura")
                    ResultadoConfiguracion.NO_ENCONTRADA ->
                        it.copy(guardando = false, errorGeneral = "No se encontró esta prueba")
                }
            }
        }
    }

    // ---------- Ayudantes ----------

    /** Después del primer intento de guardar, cada cambio vuelve a validar el formulario. */
    private fun revalidar(estado: ConfigurarPruebaUiState): ConfigurarPruebaUiState =
        if (estado.intentoEnviar) conErrores(estado) else estado

    private fun conErrores(estado: ConfigurarPruebaUiState): ConfigurarPruebaUiState = estado.copy(
        errorInstrucciones = Validaciones.instrucciones(estado.instrucciones),
        errorIntentos = Validaciones.intentos(estado.intentos),
        errorFechaApertura = Validaciones.fechaApertura(
            estado.fechaApertura, LocalDate.now(zona), estado.fechaAperturaGuardada
        ),
        errorFechaCierre = Validaciones.fechaCierre(
            estado.fechaApertura, estado.fechaCierre, estado.sinFechaLimite
        )
    )

    private fun aFecha(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zona).toLocalDate()

    companion object {
        fun factory(asignacionId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                ConfigurarPruebaViewModel(
                    asignacionRepository = app.container.asignacionRepository,
                    sesion = app.container.sesion,
                    asignacionId = asignacionId
                )
            }
        }
    }
}