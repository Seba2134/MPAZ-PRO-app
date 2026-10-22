package cl.duoc.mpazpro.ui.utp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CrearUsuarioRoute(
    onVolver: () -> Unit,
    viewModel: CrearUsuarioViewModel = viewModel(factory = CrearUsuarioViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Si la cuenta se crea correctamente, se vuelve a la pantalla anterior
    LaunchedEffect(uiState.exito) {
        if (uiState.exito) {
            onVolver()
        }
    }

    CrearUsuarioScreen(
        uiState = uiState,
        onRunChange = viewModel::onRunChange,
        onNombreChange = viewModel::onNombreChange,
        onCorreoChange = viewModel::onCorreoChange,
        onUsuarioChange = viewModel::onUsuarioChange,
        onPasswordChange = viewModel::onPasswordChange,
        onAlternarMostrarPassword = viewModel::alternarMostrarPassword,
        onRolChange = viewModel::onRolChange,
        onCursoChange = viewModel::onCursoChange,
        onGuardar = viewModel::guardarUsuario,
        onVolver = onVolver
    )
}