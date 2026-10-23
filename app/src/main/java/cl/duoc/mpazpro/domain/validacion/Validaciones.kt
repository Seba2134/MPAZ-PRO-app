package cl.duoc.mpazpro.domain.validacion

import cl.duoc.mpazpro.domain.model.TipoContenido

/**
 * Reglas de validación de los formularios.
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 * No dependen de Android ni de Compose: se pueden probar con pruebas unitarias.
 */
object Validaciones {

    const val LARGO_MINIMO_PASSWORD = 4
    private const val LARGO_MINIMO_USUARIO = 3

    const val LARGO_MAXIMO_TEXTO_CONTENIDO = 500
    const val ORDEN_MAXIMO = 99

    fun nombreUsuario(valor: String): String? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() -> "Escribe el usuario"
            limpio.contains(' ') -> "El usuario no lleva espacios"
            limpio.length < LARGO_MINIMO_USUARIO -> "El usuario tiene al menos $LARGO_MINIMO_USUARIO letras"
            else -> null
        }
    }

    fun password(valor: String): String? = when {
        valor.isEmpty() -> "Escribe la contraseña"
        valor.length < LARGO_MINIMO_PASSWORD -> "La contraseña tiene al menos $LARGO_MINIMO_PASSWORD caracteres"
        else -> null
    }

    // ---------- Crear contenido ----------

    fun objetivoSeleccionado(oaId: Long?): String? =
        if (oaId == null) "Elige el objetivo de aprendizaje" else null

    /** El texto es obligatorio en Texto y Ejemplo; en Imagen y Audio es opcional (pie o descripción). */
    fun textoContenido(tipo: TipoContenido, texto: String): String? {
        val limpio = texto.trim()
        val obligatorio = tipo == TipoContenido.TEXTO || tipo == TipoContenido.EJEMPLO
        return when {
            obligatorio && limpio.isEmpty() -> "Escribe el texto del contenido"
            limpio.length > LARGO_MAXIMO_TEXTO_CONTENIDO ->
                "Máximo $LARGO_MAXIMO_TEXTO_CONTENIDO caracteres (llevas ${limpio.length})"
            else -> null
        }
    }

    /** Una Imagen necesita una foto y un Audio necesita una grabación. */
    fun recursoContenido(tipo: TipoContenido, rutaFoto: String?, rutaAudio: String?): String? = when {
        tipo == TipoContenido.IMAGEN && rutaFoto == null -> "Toma una foto para este contenido"
        tipo == TipoContenido.AUDIO && rutaAudio == null -> "Graba el audio de este contenido"
        else -> null
    }

    /** El orden llega como texto desde el formulario. */
    fun orden(valor: String): String? {
        val limpio = valor.trim()
        val numero = limpio.toIntOrNull()
        return when {
            limpio.isEmpty() -> "Escribe el orden"
            numero == null -> "Escribe solo números"
            numero !in 1..ORDEN_MAXIMO -> "Debe ser un número entre 1 y $ORDEN_MAXIMO"
            else -> null
        }
    }

    /** La autoría es obligatoria (regla del caso: validar autoría y licencia). */
    fun autor(valor: String): String? =
        if (valor.trim().length < 2) "Escribe el autor del contenido" else null
}