package cl.duoc.mpazpro

import cl.duoc.mpazpro.data.remote.dto.ActualizarObservacionDto
import cl.duoc.mpazpro.data.remote.dto.IntentoServidorDto
import cl.duoc.mpazpro.data.remote.dto.NuevaObservacionDto
import cl.duoc.mpazpro.data.remote.dto.ObservacionDto
import cl.duoc.mpazpro.data.repository.AccionObservacion
import cl.duoc.mpazpro.data.repository.accionParaGuardar
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** La decisión POST/PUT y el mapeo de los DTO de observaciones (sin red ni Android). */
class ObservacionRemotaTest {

    private val existente = ObservacionDto(
        id = 7,
        estudianteNombreUsuario = "tomas",
        unidadTitulo = "Unidad 1: Leemos cuentos",
        texto = "Reconoce bien",
        validado = false,
        docenteNombreUsuario = "ana",
        fechaActualizacion = 1_764_000_000_000
    )

    @Test
    fun siNoExisteEnElServidorSeCrea() {
        val accion = accionParaGuardar(null, "tomas", "Unidad 1: Leemos cuentos", "Muy bien", validado = false)

        assertEquals(
            AccionObservacion.Crear(NuevaObservacionDto("tomas", "Unidad 1: Leemos cuentos", "Muy bien")),
            accion
        )
    }

    @Test
    fun siYaExisteSeActualizaConSuId() {
        val accion = accionParaGuardar(existente, "tomas", "Unidad 1: Leemos cuentos", "Reconoce bien", validado = true)

        assertEquals(AccionObservacion.Actualizar(7, ActualizarObservacionDto("Reconoce bien", true)), accion)
    }

    @Test
    fun laRespuestaDelServidorSeLeeCompleta() {
        val json = """
            {"id":7,"estudianteNombreUsuario":"tomas","unidadTitulo":"Unidad 1: Leemos cuentos",
             "texto":"Reconoce bien","validado":false,"docenteNombreUsuario":"ana","fechaActualizacion":1764000000000}
        """.trimIndent()

        assertEquals(existente, Gson().fromJson(json, ObservacionDto::class.java))
    }

    @Test
    fun loQueSeEnviaTieneLosNombresQueEsperaElServidor() {
        val nueva = Gson().toJson(NuevaObservacionDto("tomas", "U1", "Hola"))
        val cambio = Gson().toJson(ActualizarObservacionDto("Hola", true))

        assertTrue(nueva.contains("\"estudianteNombreUsuario\":\"tomas\""))
        assertTrue(nueva.contains("\"unidadTitulo\":\"U1\""))
        assertEquals("{\"texto\":\"Hola\",\"validado\":true}", cambio)
    }

    @Test
    fun losIntentosDelServidorIgnoranCamposQueLaAppNoUsa() {
        val json = """
            [{"id":1,"estudianteNombreUsuario":"tomas","unidadTitulo":"U1","actividadId":2,
              "actividadTitulo":"Prueba final","tipo":"PRUEBA_FINAL","numeroIntento":1,
              "fechaInicio":1,"fechaFin":5,"correctas":3,"total":4,"respuestas":[]}]
        """.trimIndent()

        val intentos = Gson().fromJson(json, Array<IntentoServidorDto>::class.java).toList()

        assertEquals(1, intentos.size)
        assertEquals(IntentoServidorDto(1, "U1", "Prueba final", "PRUEBA_FINAL", 1, 5, 3, 4), intentos.first())
    }
}
