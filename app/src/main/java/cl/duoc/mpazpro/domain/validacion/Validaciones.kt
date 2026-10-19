package cl.duoc.mpazpro.domain.validacion

/**
 * Reglas de validación de los formularios.
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 * No dependen de Android ni de Compose: se pueden probar con pruebas unitarias.
 */
object Validaciones {

    const val LARGO_MINIMO_PASSWORD = 4
    private const val LARGO_MINIMO_USUARIO = 3

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
}