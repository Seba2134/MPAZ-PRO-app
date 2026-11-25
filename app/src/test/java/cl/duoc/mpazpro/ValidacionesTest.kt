package cl.duoc.mpazpro

import cl.duoc.mpazpro.domain.model.Rol
import cl.duoc.mpazpro.domain.model.TipoContenido
import cl.duoc.mpazpro.domain.validacion.Validaciones
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** Cada regla: un caso válido (null) y cada caso de error (mensaje). */
class ValidacionesTest {

    private val hoy: LocalDate = LocalDate.of(2026, 11, 24)

    // ---------- Login y cuentas ----------

    @Test
    fun nombreUsuario() {
        assertNull(Validaciones.nombreUsuario("tomas"))
        assertNotNull(Validaciones.nombreUsuario(""))
        assertNotNull(Validaciones.nombreUsuario("to mas"))
        assertNotNull(Validaciones.nombreUsuario("to"))
    }

    @Test
    fun password() {
        assertNull(Validaciones.password("1234"))
        assertNotNull(Validaciones.password(""))
        assertNotNull(Validaciones.password("123"))
    }

    @Test
    fun nombre() {
        assertNull(Validaciones.nombre("Tomás"))
        assertNotNull(Validaciones.nombre("   "))
        assertNotNull(Validaciones.nombre("T"))
    }

    @Test
    fun correo() {
        assertNull(Validaciones.correo(""))
        assertNull(Validaciones.correo("ana@colegio.cl"))
        assertNotNull(Validaciones.correo("ana@colegio"))
    }

    @Test
    fun cursoDelEstudiante() {
        assertNull(Validaciones.cursoDelEstudiante(Rol.ESTUDIANTE, 1L))
        assertNull(Validaciones.cursoDelEstudiante(Rol.DOCENTE, null))
        assertNotNull(Validaciones.cursoDelEstudiante(Rol.ESTUDIANTE, null))
    }

    // ---------- Configurar prueba ----------

    @Test
    fun instrucciones() {
        assertNull(Validaciones.instrucciones("Lee con calma"))
        assertNotNull(Validaciones.instrucciones(""))
        assertNotNull(Validaciones.instrucciones("a".repeat(Validaciones.LARGO_MAXIMO_INSTRUCCIONES + 1)))
    }

    @Test
    fun intentos() {
        assertNull(Validaciones.intentos("2"))
        assertNotNull(Validaciones.intentos(""))
        assertNotNull(Validaciones.intentos("dos"))
        assertNotNull(Validaciones.intentos("0"))
        assertNotNull(Validaciones.intentos("${Validaciones.INTENTOS_MAXIMOS + 1}"))
    }

    @Test
    fun fechaApertura() {
        assertNull(Validaciones.fechaApertura(hoy, hoy, null))
        assertNotNull(Validaciones.fechaApertura(null, hoy, null))
        assertNotNull(Validaciones.fechaApertura(hoy.minusDays(1), hoy, null))
        // Si ya estaba guardada (la prueba ya abrió), se puede mantener aunque sea pasada
        assertNull(Validaciones.fechaApertura(hoy.minusDays(1), hoy, hoy.minusDays(1)))
    }

    @Test
    fun fechaCierre() {
        assertNull(Validaciones.fechaCierre(hoy, hoy.plusDays(3), sinFechaLimite = false))
        assertNull(Validaciones.fechaCierre(hoy, null, sinFechaLimite = true))
        assertNotNull(Validaciones.fechaCierre(hoy, null, sinFechaLimite = false))
        // Cierre antes de la apertura
        assertNotNull(Validaciones.fechaCierre(hoy, hoy.minusDays(1), sinFechaLimite = false))
    }

    // ---------- Crear contenido ----------

    @Test
    fun objetivoSeleccionado() {
        assertNull(Validaciones.objetivoSeleccionado(1L))
        assertNotNull(Validaciones.objetivoSeleccionado(null))
    }

    @Test
    fun textoContenido() {
        assertNull(Validaciones.textoContenido(TipoContenido.TEXTO, "Un cuento tiene personajes"))
        assertNull(Validaciones.textoContenido(TipoContenido.IMAGEN, ""))
        assertNotNull(Validaciones.textoContenido(TipoContenido.EJEMPLO, " "))
        assertNotNull(Validaciones.textoContenido(TipoContenido.TEXTO, "a".repeat(Validaciones.LARGO_MAXIMO_TEXTO_CONTENIDO + 1)))
    }

    @Test
    fun recursoContenido() {
        assertNull(Validaciones.recursoContenido(TipoContenido.IMAGEN, "/foto.jpg", null))
        assertNull(Validaciones.recursoContenido(TipoContenido.TEXTO, null, null))
        assertNotNull(Validaciones.recursoContenido(TipoContenido.IMAGEN, null, null))
        assertNotNull(Validaciones.recursoContenido(TipoContenido.AUDIO, null, null))
    }

    @Test
    fun orden() {
        assertNull(Validaciones.orden("1"))
        assertNotNull(Validaciones.orden(""))
        assertNotNull(Validaciones.orden("x"))
        assertNotNull(Validaciones.orden("0"))
    }

    @Test
    fun autor() {
        assertNull(Validaciones.autor("Jefatura UTP"))
        assertNotNull(Validaciones.autor("J"))
    }

    // ---------- Cursos ----------

    @Test
    fun nombreCurso() {
        assertNull(Validaciones.nombreCurso("2.º B"))
        assertNotNull(Validaciones.nombreCurso(""))
        assertNotNull(Validaciones.nombreCurso("a".repeat(Validaciones.LARGO_MAXIMO_NOMBRE_CURSO + 1)))
    }

    @Test
    fun profesorJefe() {
        assertNull(Validaciones.profesorJefe(2L))
        assertNotNull(Validaciones.profesorJefe(null))
    }

    // ---------- Informe ----------

    @Test
    fun observacion() {
        assertNull(Validaciones.observacion("Reconoce bien los personajes"))
        assertNotNull(Validaciones.observacion("  "))
        assertNotNull(Validaciones.observacion("a".repeat(Validaciones.LARGO_MAXIMO_OBSERVACION + 1)))
    }

    // ---------- Unidades y OA ----------

    @Test
    fun tituloUnidad() {
        assertNull(Validaciones.tituloUnidad("Las plantas"))
        assertNotNull(Validaciones.tituloUnidad(""))
        assertNotNull(Validaciones.tituloUnidad("a".repeat(Validaciones.LARGO_MAXIMO_TITULO_UNIDAD + 1)))
    }

    @Test
    fun codigoOa() {
        assertNull(Validaciones.codigoOa("OA 3"))
        assertNotNull(Validaciones.codigoOa(""))
        assertNotNull(Validaciones.codigoOa("a".repeat(Validaciones.LARGO_MAXIMO_CODIGO_OA + 1)))
    }

    @Test
    fun descripcionOa() {
        assertNull(Validaciones.descripcionOa("Reconocer las partes de una planta"))
        assertNotNull(Validaciones.descripcionOa(""))
        assertNotNull(Validaciones.descripcionOa("a".repeat(Validaciones.LARGO_MAXIMO_DESCRIPCION_OA + 1)))
    }

    @Test
    fun comentarioCambio() {
        assertNull(Validaciones.comentarioCambio(""))
        assertNull(Validaciones.comentarioCambio("Falta el audio del OA 2"))
        assertNotNull(Validaciones.comentarioCambio("a".repeat(Validaciones.LARGO_MAXIMO_COMENTARIO + 1)))
    }

    // ---------- Actividades y preguntas ----------

    @Test
    fun tituloEInstruccionDeActividad() {
        assertNull(Validaciones.tituloActividad("Desafío: las hojas"))
        assertNotNull(Validaciones.tituloActividad(""))
        assertNull(Validaciones.instruccionActividad("Toca la respuesta correcta"))
        assertNotNull(Validaciones.instruccionActividad(""))
    }

    @Test
    fun enunciadoPregunta() {
        assertNull(Validaciones.enunciadoPregunta("¿Qué parte absorbe el agua?"))
        assertNotNull(Validaciones.enunciadoPregunta(""))
        assertNotNull(Validaciones.enunciadoPregunta("a".repeat(Validaciones.LARGO_MAXIMO_ENUNCIADO + 1)))
    }

    @Test
    fun alternativasPregunta() {
        assertNull(Validaciones.alternativasPregunta(listOf("Raíz", "Hoja", "", ""), correcta = 0))
        // Solo una alternativa escrita
        assertNotNull(Validaciones.alternativasPregunta(listOf("Raíz", "", "", ""), correcta = 0))
        // Sin marcar la correcta
        assertNotNull(Validaciones.alternativasPregunta(listOf("Raíz", "Hoja", "", ""), correcta = null))
        // La correcta está vacía
        assertNotNull(Validaciones.alternativasPregunta(listOf("Raíz", "Hoja", "", ""), correcta = 2))
        // Repetidas (sin importar mayúsculas)
        assertNotNull(Validaciones.alternativasPregunta(listOf("Raíz", "raíz", "Hoja", ""), correcta = 2))
    }

    @Test
    fun preguntaCompleta() {
        assertNull(Validaciones.pregunta(1L, "¿Qué parte absorbe el agua?", listOf("Raíz", "Hoja", "", ""), 0))
        assertNotNull(Validaciones.pregunta(null, "¿Qué parte absorbe el agua?", listOf("Raíz", "Hoja", "", ""), 0))
        assertNotNull(Validaciones.pregunta(1L, "", listOf("Raíz", "Hoja", "", ""), 0))
        assertNotNull(Validaciones.pregunta(1L, "¿Qué parte absorbe el agua?", listOf("Raíz", "", "", ""), 0))
    }
}
