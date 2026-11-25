package cl.duoc.mpazpro.data.local.relation

import cl.duoc.mpazpro.domain.model.EstadoContenido

/** Un cambio de estado de una unidad con el nombre de quien lo hizo. No es una tabla. */
data class HistorialConUsuario(
    val id: Long,
    val estadoAnterior: EstadoContenido?,
    val estadoNuevo: EstadoContenido,
    val usuarioNombre: String?,
    val comentario: String?,
    val fecha: Long
)
