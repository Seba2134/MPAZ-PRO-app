package cl.duoc.mpazpro.ui.docente

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoFecha
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.components.MensajeError
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Cuál calendario está abierto (o ninguno). */
private enum class CalendarioAbierto { APERTURA, CIERRE }

/** Versión "conectada": crea el ViewModel para esta prueba y vuelve atrás al guardar. */
@Composable
fun ConfigurarPruebaRoute(
    asignacionId: Long,
    onVolver: () -> Unit,
    viewModel: ConfigurarPruebaViewModel = viewModel(factory = ConfigurarPruebaViewModel.factory(asignacionId))
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.guardadoOk) {
        if (uiState.guardadoOk) onVolver()
    }

    ConfigurarPruebaScreen(
        uiState = uiState,
        onInstruccionesChange = viewModel::onInstruccionesChange,
        onIntentosChange = viewModel::onIntentosChange,
        onFechaAperturaChange = viewModel::onFechaAperturaChange,
        onFechaCierreChange = viewModel::onFechaCierreChange,
        onSinFechaLimiteChange = viewModel::onSinFechaLimiteChange,
        onMostrarRespuestasChange = viewModel::onMostrarRespuestasChange,
        onGuardar = viewModel::guardar,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarPruebaScreen(
    uiState: ConfigurarPruebaUiState,
    onInstruccionesChange: (String) -> Unit,
    onIntentosChange: (String) -> Unit,
    onFechaAperturaChange: (LocalDate) -> Unit,
    onFechaCierreChange: (LocalDate) -> Unit,
    onSinFechaLimiteChange: (Boolean) -> Unit,
    onMostrarRespuestasChange: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    var calendario by remember { mutableStateOf<CalendarioAbierto?>(null) }
    val hoy = remember { LocalDate.now() }
    val habilitado = !uiState.guardando

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar prueba") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.cargandoDatos) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- Encabezado: qué prueba se está configurando ----------
            Text(uiState.unidad, style = MaterialTheme.typography.titleLarge)
            Text("${uiState.asignatura} · ${uiState.curso}", style = MaterialTheme.typography.bodyMedium)

            // ---------- String: instrucciones ----------
            CampoTexto(
                valor = uiState.instrucciones,
                onValorChange = onInstruccionesChange,
                etiqueta = "Instrucciones para los estudiantes",
                icono = Icons.Filled.Edit,
                error = uiState.errorInstrucciones,
                enabled = habilitado,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // ---------- Int: cantidad de intentos ----------
            CampoTexto(
                valor = uiState.intentos,
                onValorChange = onIntentosChange,
                etiqueta = "Cantidad de intentos (1 a 5)",
                icono = Icons.Filled.Refresh,
                error = uiState.errorIntentos,
                enabled = habilitado,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )

            // ---------- Date: apertura ----------
            CampoFecha(
                etiqueta = "Fecha de apertura",
                fecha = uiState.fechaApertura,
                error = uiState.errorFechaApertura,
                enabled = habilitado,
                onClick = { calendario = CalendarioAbierto.APERTURA }
            )

            // ---------- Switch: sin fecha límite ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sin fecha límite",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.sinFechaLimite,
                    onCheckedChange = onSinFechaLimiteChange,
                    enabled = habilitado
                )
            }

            // ---------- Date: cierre (solo si hay fecha límite) ----------
            AnimatedVisibility(
                visible = !uiState.sinFechaLimite,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CampoFecha(
                    etiqueta = "Fecha de cierre",
                    fecha = uiState.fechaCierre,
                    error = uiState.errorFechaCierre,
                    enabled = habilitado,
                    onClick = { calendario = CalendarioAbierto.CIERRE }
                )
            }

            // ---------- Checkbox: mostrar respuestas ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = uiState.mostrarRespuestas,
                        enabled = habilitado,
                        role = Role.Checkbox,
                        onValueChange = onMostrarRespuestasChange
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.mostrarRespuestas,
                    onCheckedChange = null,
                    enabled = habilitado
                )
                Text(
                    text = "Mostrar las respuestas correctas al terminar",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // ---------- Error general y botón ----------
            MensajeError(uiState.errorGeneral)

            BotonGrande(
                texto = if (uiState.guardando) "Guardando..." else "Guardar configuración",
                onClick = onGuardar,
                cargando = uiState.guardando
            )
        }
    }

    // ---------- Calendarios ----------
    when (calendario) {
        CalendarioAbierto.APERTURA -> SelectorFecha(
            fechaInicial = uiState.fechaApertura,
            // Se permite hoy en adelante, o la fecha ya guardada si la prueba ya abrió
            fechaMinima = uiState.fechaAperturaGuardada?.takeIf { it.isBefore(hoy) } ?: hoy,
            onConfirmar = {
                onFechaAperturaChange(it)
                calendario = null
            },
            onCancelar = { calendario = null }
        )
        CalendarioAbierto.CIERRE -> SelectorFecha(
            fechaInicial = uiState.fechaCierre ?: uiState.fechaApertura,
            fechaMinima = uiState.fechaApertura ?: hoy,
            onConfirmar = {
                onFechaCierreChange(it)
                calendario = null
            },
            onCancelar = { calendario = null }
        )
        null -> Unit
    }
}

/**
 * Calendario de Material 3 en un diálogo. Los días anteriores a [fechaMinima] aparecen deshabilitados.
 * El DatePicker trabaja en UTC, por eso las conversiones usan ZoneOffset.UTC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    fechaInicial: LocalDate?,
    fechaMinima: LocalDate,
    onConfirmar: (LocalDate) -> Unit,
    onCancelar: () -> Unit
) {
    val minimoUtc = fechaMinima.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    val estado = rememberDatePickerState(
        initialSelectedDateMillis = fechaInicial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= minimoUtc
        }
    )

    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        onConfirmar(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = estado.selectedDateMillis != null
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = estado)
    }
}