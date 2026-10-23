package cl.duoc.mpazpro.ui.utp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.domain.model.Licencia
import cl.duoc.mpazpro.domain.model.TipoContenido
import cl.duoc.mpazpro.ui.components.BotonGrande
import cl.duoc.mpazpro.ui.components.CampoTexto
import cl.duoc.mpazpro.ui.components.MensajeError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class EstadoPermiso {
    SIN_PROBLEMA,
    RECHAZADO,
    RECHAZADO_PERMANENTE
}

@Composable
fun CrearContenidoRoute(
    onVolver: () -> Unit,
    viewModel: CrearContenidoViewModel = viewModel(factory = CrearContenidoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.buscarActivity() }
    val hayCamara = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }

    var permisoCamara by rememberSaveable { mutableStateOf(EstadoPermiso.SIN_PROBLEMA) }
    var permisoMicrofono by rememberSaveable { mutableStateOf(EstadoPermiso.SIN_PROBLEMA) }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        viewModel.onFotoTomada(exito)
    }

    val lanzadorPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            permisoCamara = EstadoPermiso.SIN_PROBLEMA
            lanzadorCamara.launch(viewModel.prepararFoto())
        } else {
            permisoCamara = estadoTrasRechazo(activity, Manifest.permission.CAMERA)
        }
    }

    val lanzadorPermisoMicrofono = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            permisoMicrofono = EstadoPermiso.SIN_PROBLEMA
            viewModel.iniciarGrabacion()
        } else {
            permisoMicrofono = estadoTrasRechazo(activity, Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(uiState.guardadoOk) {
        if (uiState.guardadoOk) onVolver()
    }

    CrearContenidoScreen(
        uiState = uiState,
        hayCamara = hayCamara,
        permisoCamara = permisoCamara,
        permisoMicrofono = permisoMicrofono,
        onAsignaturaChange = viewModel::onAsignaturaChange,
        onOaChange = viewModel::onOaChange,
        onTipoChange = viewModel::onTipoChange,
        onTextoChange = viewModel::onTextoChange,
        onOrdenChange = viewModel::onOrdenChange,
        onAutorChange = viewModel::onAutorChange,
        onLicenciaChange = viewModel::onLicenciaChange,
        onTomarFoto = {
            if (tienePermiso(context, Manifest.permission.CAMERA)) {
                lanzadorCamara.launch(viewModel.prepararFoto())
                permisoCamara = EstadoPermiso.SIN_PROBLEMA
            } else {
                lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
            }
        },
        onGrabarODetener = {
            if (uiState.grabando) {
                viewModel.detenerGrabacion()
            } else if (tienePermiso(context, Manifest.permission.RECORD_AUDIO)) {
                permisoMicrofono = EstadoPermiso.SIN_PROBLEMA
                viewModel.iniciarGrabacion()
            } else {
                lanzadorPermisoMicrofono.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onEscuchar = viewModel::alternarReproduccion,
        onAbrirAjustes = { abrirAjustesDeLaApp(context) },
        onGuardar = viewModel::guardar,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearContenidoScreen(
    uiState: CrearContenidoUiState,
    hayCamara: Boolean,
    permisoCamara: EstadoPermiso,
    permisoMicrofono: EstadoPermiso,
    onAsignaturaChange: (Long) -> Unit,
    onOaChange: (Long) -> Unit,
    onTipoChange: (TipoContenido) -> Unit,
    onTextoChange: (String) -> Unit,
    onOrdenChange: (String) -> Unit,
    onAutorChange: (String) -> Unit,
    onLicenciaChange: (Licencia) -> Unit,
    onTomarFoto: () -> Unit,
    onGrabarODetener: () -> Unit,
    onEscuchar: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    val habilitado = !uiState.guardando

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear contenido") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Asignatura", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.asignaturas.forEach { asignatura ->
                    FilterChip(
                        selected = asignatura.id == uiState.asignaturaId,
                        onClick = { onAsignaturaChange(asignatura.id) },
                        label = { Text(asignatura.nombre) },
                        enabled = habilitado
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.asignaturaId != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Objetivo de aprendizaje", style = MaterialTheme.typography.bodyLarge)
                    uiState.objetivosDeLaAsignatura.forEach { oa ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = oa.id == uiState.oaId,
                                    enabled = habilitado,
                                    role = Role.RadioButton,
                                    onClick = { onOaChange(oa.id) }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = oa.id == uiState.oaId, onClick = null, enabled = habilitado)
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("${oa.codigo}: ${oa.descripcion}", style = MaterialTheme.typography.bodyMedium)
                                Text(oa.unidadTitulo, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            MensajeError(uiState.errorOa)

            Text("Tipo de contenido", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoContenido.entries.forEach { tipo ->
                    FilterChip(
                        selected = tipo == uiState.tipo,
                        onClick = { onTipoChange(tipo) },
                        label = { Text(tipo.nombreVisible) },
                        enabled = habilitado
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.tipo == TipoContenido.IMAGEN,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.rutaFoto?.let { VistaPreviaFoto(ruta = it) }
                    if (hayCamara) {
                        BotonGrande(
                            texto = if (uiState.rutaFoto == null) "Tomar foto" else "Tomar otra foto",
                            onClick = onTomarFoto,
                            enabled = habilitado
                        )
                    } else {
                        Text(
                            text = "Este equipo no tiene cámara disponible.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    AvisoPermiso(
                        estado = permisoCamara,
                        explicacion = "La cámara se usa para fotografiar láminas y materiales que verán los estudiantes.",
                        onReintentar = onTomarFoto,
                        onAbrirAjustes = onAbrirAjustes
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.tipo == TipoContenido.AUDIO,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedVisibility(visible = uiState.grabando) {
                        IndicadorGrabando()
                    }
                    BotonGrande(
                        texto = when {
                            uiState.grabando -> "Detener grabación"
                            uiState.rutaAudio == null -> "Grabar audio"
                            else -> "Grabar de nuevo"
                        },
                        onClick = onGrabarODetener,
                        enabled = habilitado,
                        colorFondo = if (uiState.grabando) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    AnimatedVisibility(visible = uiState.rutaAudio != null && !uiState.grabando) {
                        BotonGrande(
                            texto = if (uiState.reproduciendo) "Detener audio" else "Escuchar grabación",
                            onClick = onEscuchar,
                            enabled = habilitado,
                            colorFondo = MaterialTheme.colorScheme.secondary
                        )
                    }
                    AvisoPermiso(
                        estado = permisoMicrofono,
                        explicacion = "El micrófono se usa para grabar la explicación que escucharán los estudiantes.",
                        onReintentar = onGrabarODetener,
                        onAbrirAjustes = onAbrirAjustes
                    )
                }
            }
            MensajeError(uiState.errorRecurso)

            CampoTexto(
                valor = uiState.texto,
                onValorChange = onTextoChange,
                etiqueta = when (uiState.tipo) {
                    TipoContenido.TEXTO -> "Texto del contenido"
                    TipoContenido.EJEMPLO -> "Texto del ejemplo"
                    TipoContenido.IMAGEN -> "Pie de foto (opcional)"
                    TipoContenido.AUDIO -> "Descripción del audio (opcional)"
                },
                icono = Icons.Filled.Create,
                error = uiState.errorTexto,
                enabled = habilitado,
                multilinea = true
            )

            CampoTexto(
                valor = uiState.orden,
                onValorChange = onOrdenChange,
                etiqueta = "Orden dentro del objetivo",
                icono = Icons.AutoMirrored.Filled.List,
                error = uiState.errorOrden,
                enabled = habilitado,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            CampoTexto(
                valor = uiState.autor,
                onValorChange = onAutorChange,
                etiqueta = "Autor",
                icono = Icons.Filled.Person,
                error = uiState.errorAutor,
                enabled = habilitado,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Text("Licencia", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Licencia.entries.forEach { licencia ->
                    FilterChip(
                        selected = licencia == uiState.licencia,
                        onClick = { onLicenciaChange(licencia) },
                        label = { Text(licencia.nombreVisible) },
                        enabled = habilitado
                    )
                }
            }

            MensajeError(uiState.errorGeneral)

            BotonGrande(
                texto = if (uiState.guardando) "Guardando..." else "Guardar contenido",
                onClick = onGuardar,
                cargando = uiState.guardando
            )
        }
    }
}

@Composable
private fun AvisoPermiso(
    estado: EstadoPermiso,
    explicacion: String,
    onReintentar: () -> Unit,
    onAbrirAjustes: () -> Unit
) {
    AnimatedVisibility(
        visible = estado != EstadoPermiso.SIN_PROBLEMA,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val permanente = estado == EstadoPermiso.RECHAZADO_PERMANENTE
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Text(
                        text = "Permiso necesario",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = if (permanente) "$explicacion Para usarla, actívalo en los ajustes de la app." else explicacion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = if (permanente) onAbrirAjustes else onReintentar) {
                    Text(if (permanente) "Abrir ajustes" else "Intentar de nuevo")
                }
            }
        }
    }
}

@Composable
private fun IndicadorGrabando() {
    val transicion = rememberInfiniteTransition(label = "grabando")
    val alfa by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 600), RepeatMode.Reverse),
        label = "alfaPunto"
    )
    var segundos by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            segundos++
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .alpha(alfa)
                .background(MaterialTheme.colorScheme.error, CircleShape)
        )
        Text(
            text = "Grabando... %d:%02d".format(segundos / 60, segundos % 60),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun VistaPreviaFoto(ruta: String) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) { cargarMiniatura(ruta, ladoMaximo = 1024) }
    }
    imagen?.let {
        Image(
            bitmap = it,
            contentDescription = "Foto tomada para el contenido",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

private fun cargarMiniatura(ruta: String, ladoMaximo: Int): ImageBitmap? {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, medidas)
    if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return null
    var escala = 1
    while (medidas.outWidth / escala > ladoMaximo || medidas.outHeight / escala > ladoMaximo) {
        escala *= 2
    }
    val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
    return BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
}

private fun tienePermiso(context: Context, permiso: String): Boolean =
    ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED

private fun estadoTrasRechazo(activity: Activity?, permiso: String): EstadoPermiso =
    if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permiso)) {
        EstadoPermiso.RECHAZADO
    } else {
        EstadoPermiso.RECHAZADO_PERMANENTE
    }

private fun abrirAjustesDeLaApp(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private tailrec fun Context.buscarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActivity()
    else -> null
}