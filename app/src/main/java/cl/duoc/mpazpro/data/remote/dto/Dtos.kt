package cl.duoc.mpazpro.data.remote.dto

/** Lo que se manda para iniciar sesión en el servidor. */
data class LoginDto(
    val nombreUsuario: String,
    val password: String
)

/** Lo que responde el servidor al iniciar sesión. [expiraEn] es epoch en milisegundos. */
data class TokenDto(
    val token: String,
    val rol: String,
    val nombre: String,
    val expiraEn: Long
)

/** Un intento terminado, con los mismos campos que espera POST /api/intentos. */
data class IntentoDto(
    val estudianteNombreUsuario: String,
    val estudianteNombre: String,
    val cursoNombre: String,
    val asignaturaNombre: String,
    val unidadTitulo: String,
    val actividadId: Long,
    val actividadTitulo: String,
    val tipo: String,
    val numeroIntento: Int,
    val fechaInicio: Long,
    val fechaFin: Long,
    val correctas: Int,
    val total: Int,
    val respuestas: List<RespuestaDto>
)

data class RespuestaDto(
    val preguntaId: Long,
    val enunciado: String,
    val oaCodigo: String,
    val oaDescripcion: String,
    val esCorrecta: Boolean
)

/** Una observación tal como la devuelve ms-resultados. [fechaActualizacion] es epoch en milisegundos. */
data class ObservacionDto(
    val id: Long,
    val estudianteNombreUsuario: String,
    val unidadTitulo: String,
    val texto: String,
    val validado: Boolean,
    val docenteNombreUsuario: String,
    val fechaActualizacion: Long
)

/** Para crear una observación (POST). Nace sin validar. */
data class NuevaObservacionDto(
    val estudianteNombreUsuario: String,
    val unidadTitulo: String,
    val texto: String
)

/** Para actualizar una observación (PUT). Si el texto cambia, el servidor quita la validación. */
data class ActualizarObservacionDto(
    val texto: String,
    val validado: Boolean
)

/** Un intento guardado en ms-resultados (solo los campos que muestra la app). */
data class IntentoServidorDto(
    val id: Long,
    val unidadTitulo: String,
    val actividadTitulo: String,
    val tipo: String,
    val numeroIntento: Int,
    val fechaFin: Long,
    val correctas: Int,
    val total: Int
)
