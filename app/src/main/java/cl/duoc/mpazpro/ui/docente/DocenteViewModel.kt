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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Inicio del docente: las pruebas de sus cursos que puede configurar. */
class DocenteViewModel(
    asignacionRepository: AsignacionRepository,
    docenteId: Long
) : ViewModel() {

    val pruebas: StateFlow<List<PruebaDocente>> =
        asignacionRepository.pruebasDelDocente(docenteId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    companion object {
        fun factory(docenteId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MpazApp
                DocenteViewModel(app.container.asignacionRepository, docenteId)
            }
        }
    }
}