package cl.duoc.mpazpro

import cl.duoc.mpazpro.data.local.relation.IntentoParaEnviar
import cl.duoc.mpazpro.data.local.relation.RespuestaParaEnviar
import cl.duoc.mpazpro.data.remote.dto.IntentoDto
import cl.duoc.mpazpro.data.remote.dto.RespuestaDto
import cl.duoc.mpazpro.data.repository.SincronizacionRepository
import cl.duoc.mpazpro.domain.model.TipoActividad
import org.junit.Assert.assertEquals
import org.junit.Test

class SincronizacionMapeoTest {

    @Test
    fun aDtoCopiaTodosLosCamposYLasRespuestas() {
        val intento = IntentoParaEnviar(
            intentoId = 7,
            estudianteNombreUsuario = "tomas",
            estudianteNombre = "Tomás",
            cursoNombre = "2.º A",
            asignaturaNombre = "Lenguaje y Comunicación",
            unidadTitulo = "Unidad 1: Leemos cuentos",
            actividadId = 2,
            actividadTitulo = "Prueba final: Leemos cuentos",
            tipo = TipoActividad.PRUEBA_FINAL,
            numeroIntento = 1,
            fechaInicio = 1_764_000_000_000,
            fechaFin = 1_764_000_300_000,
            correctas = 3,
            totalPreguntas = 4
        )
        val respuestas = listOf(
            RespuestaParaEnviar(3, "¿Qué es un personaje?", "OA 1", "Identificar personajes", true),
            RespuestaParaEnviar(4, "¿Cuál es el lugar?", "OA 1", "Identificar personajes", false)
        )

        val dto = SincronizacionRepository.aDto(intento, respuestas)

        val esperado = IntentoDto(
            estudianteNombreUsuario = "tomas",
            estudianteNombre = "Tomás",
            cursoNombre = "2.º A",
            asignaturaNombre = "Lenguaje y Comunicación",
            unidadTitulo = "Unidad 1: Leemos cuentos",
            actividadId = 2,
            actividadTitulo = "Prueba final: Leemos cuentos",
            tipo = "PRUEBA_FINAL",
            numeroIntento = 1,
            fechaInicio = 1_764_000_000_000,
            fechaFin = 1_764_000_300_000,
            correctas = 3,
            total = 4,
            respuestas = listOf(
                RespuestaDto(3, "¿Qué es un personaje?", "OA 1", "Identificar personajes", true),
                RespuestaDto(4, "¿Cuál es el lugar?", "OA 1", "Identificar personajes", false)
            )
        )
        assertEquals(esperado, dto)
    }

    @Test
    fun unDesafioSinRespuestasSeEnviaConListaVacia() {
        val intento = IntentoParaEnviar(
            1, "sofia", "Sofía", "2.º A", "Matemática", "Unidad 1", 5, "Desafío", TipoActividad.DESAFIO,
            2, 10, 20, 0, 0
        )

        val dto = SincronizacionRepository.aDto(intento, emptyList())

        assertEquals("DESAFIO", dto.tipo)
        assertEquals(2, dto.numeroIntento)
        assertEquals(0, dto.total)
        assertEquals(emptyList<RespuestaDto>(), dto.respuestas)
    }
}
