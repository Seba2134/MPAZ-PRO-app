package cl.duoc.mpazpro.data.local.relation

/**
 * Una respuesta de un intento con todo lo necesario para mostrarla:
 * la pregunta, su OA, lo que eligió el estudiante y la respuesta correcta. No es una tabla.
 */
data class RespuestaDetalle(
    val preguntaId: Long,
    val enunciado: String,
    val oaCodigo: String,
    val respuestaElegida: String?,
    val respuestaCorrecta: String?,
    val esCorrecta: Boolean
)
