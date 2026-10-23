package cl.duoc.mpazpro.domain.validacion

import cl.duoc.mpazpro.domain.model.Rol
import java.time.LocalDate
import cl.duoc.mpazpro.domain.model.TipoContenido
/**
 * Reglas de validación de los formularios.
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 * No dependen de Android ni de Compose: se pueden probar con pruebas unitarias.
 */
object Validaciones {

    // ---------- Constantes ----------
    const val LARGO_MINIMO_PASSWORD = 4
    const val INTENTOS_MINIMOS = 1
    const val INTENTOS_MAXIMOS = 5
    const val LARGO_MAXIMO_INSTRUCCIONES = 120
    private const val LARGO_MINIMO_USUARIO = 3
    private const val LARGO_MINIMO_NOMBRE = 2
    const val LARGO_MAXIMO_TEXTO_CONTENIDO = 500
    const val ORDEN_MAXIMO = 99
    const val LARGO_MAXIMO_NOMBRE_CURSO = 20

    private val PATRON_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // ---------- Login y cuentas ----------

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

    // ---------- Configurar prueba ----------
    fun instrucciones(valor: String): String? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() -> "Escribe las instrucciones para tus estudiantes"
            limpio.length > LARGO_MAXIMO_INSTRUCCIONES ->
                "Máximo $LARGO_MAXIMO_INSTRUCCIONES caracteres (llevas ${limpio.length})"
            else -> null
        }
    }

    /** Los intentos llegan como texto desde el formulario: se valida que sea un número en el rango. */
    fun intentos(valor: String): String? {
        val limpio = valor.trim()
        val numero = limpio.toIntOrNull()
        return when {
            limpio.isEmpty() -> "Escribe la cantidad de intentos"
            numero == null -> "Escribe solo números"
            numero !in INTENTOS_MINIMOS..INTENTOS_MAXIMOS ->
                "Debe ser entre $INTENTOS_MINIMOS y $INTENTOS_MAXIMOS intentos"
            else -> null
        }
    }

    /**
     * La apertura es obligatoria y no puede ser pasada.
     * Excepción: si ya estaba guardada (la prueba ya abrió), se permite mantenerla.
     */
    fun fechaApertura(fecha: LocalDate?, hoy: LocalDate, fechaGuardada: LocalDate?): String? = when {
        fecha == null -> "Elige la fecha de apertura"
        fecha.isBefore(hoy) && fecha != fechaGuardada -> "La fecha de apertura no puede ser pasada"
        else -> null
    }

    /** El cierre solo se exige si NO está activado "sin fecha límite", y no puede ser antes de la apertura. */
    fun fechaCierre(apertura: LocalDate?, cierre: LocalDate?, sinFechaLimite: Boolean): String? = when {
        sinFechaLimite -> null
        cierre == null -> "Elige la fecha de cierre"
        apertura != null && cierre.isBefore(apertura) -> "El cierre no puede ser antes de la apertura"
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


    // ---------- Cursos ----------
    fun nombreCurso(valor: String): String? {
        val limpio = valor.trim()
        return when {
            limpio.isEmpty() -> "Escribe el nombre del curso"
            limpio.length > LARGO_MAXIMO_NOMBRE_CURSO -> "Máximo $LARGO_MAXIMO_NOMBRE_CURSO caracteres"
            else -> null
        }
    }

    fun profesorJefe(docenteId: Long?): String? =
        if (docenteId == null) "Elige al profesor jefe" else null
}