package cl.duoc.mpazpro.domain.validacion

import cl.duoc.mpazpro.domain.model.Rol

/**
 * Reglas de validación de los formularios.
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 * No dependen de Android ni de Compose: se pueden probar con pruebas unitarias.
 */
object Validaciones {

    const val LARGO_MINIMO_PASSWORD = 4
    private const val LARGO_MINIMO_USUARIO = 3
    private const val LARGO_MINIMO_NOMBRE = 2

    private val PATRON_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nombreUsuario(valor: String): String? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() -> "Escribe tu usuario"
            limpio.contains(' ') -> "El usuario no lleva espacios"
            limpio.length < LARGO_MINIMO_USUARIO -> "El usuario tiene al menos $LARGO_MINIMO_USUARIO letras"
            else -> null
        }
    }

    fun password(valor: String): String? = when {
        valor.isEmpty() -> "Escribe tu contraseña"
        valor.length < LARGO_MINIMO_PASSWORD -> "La contraseña tiene al menos $LARGO_MINIMO_PASSWORD caracteres"
        else -> null
    }

    /** El nombre visible de la persona: "Tomás", "Profesora Ana". */
    fun nombre(valor: String): String? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() -> "Escribe el nombre"
            limpio.length < LARGO_MINIMO_NOMBRE -> "El nombre es muy corto"
            else -> null
        }
    }

    /** El correo es opcional: si está vacío es válido, pero si se escribe debe tener formato de correo. */
    fun correo(valor: String): String? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() -> null
            !PATRON_CORREO.matches(limpio) -> "El correo no es válido (ej: nombre@colegio.cl)"
            else -> null
        }
    }

    /** Un estudiante debe pertenecer a un curso; un docente no. */
    fun cursoDelEstudiante(rol: Rol, cursoId: Long?): String? =
        if (rol == Rol.ESTUDIANTE && cursoId == null) "Elige el curso del estudiante" else null
}