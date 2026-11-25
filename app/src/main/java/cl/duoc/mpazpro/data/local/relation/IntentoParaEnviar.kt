package cl.duoc.mpazpro.data.local.relation

import cl.duoc.mpazpro.domain.model.TipoActividad

/**
 * Un intento terminado y todavía no enviado, con los nombres que necesita el servidor:
 * junta intento + usuario + curso + actividad + unidad + asignatura. No es una tabla.
 */
data class IntentoParaEnviar(
    val intentoId: Long,
    val estudianteNombreUsuario: String,
    val estudianteNombre: String,
    val cursoNombre: String,
    val asignaturaNombre: String,
    val unidadTitulo: String,
    val actividadId: Long,
    val actividadTitulo: String,
    val tipo: TipoActividad,
    val numeroIntento: Int,
    val fechaInicio: Long,
    val fechaFin: Long,
    val correctas: Int,
    val totalPreguntas: Int
)

/** Una respuesta de un intento por enviar, con la descripción de su OA. No es una tabla. */
data class RespuestaParaEnviar(
    val preguntaId: Long,
    val enunciado: String,
    val oaCodigo: String,
    val oaDescripcion: String,
    val esCorrecta: Boolean
)
