package cl.duoc.mpazpro

import cl.duoc.mpazpro.data.local.relation.RendimientoOa
import org.junit.Assert.assertEquals
import org.junit.Test

class RendimientoOaTest {

    private fun oa(correctas: Int, total: Int) =
        RendimientoOa(oaId = 1, codigo = "OA 1", descripcion = "Personajes", correctas = correctas, total = total)

    @Test
    fun porcentajeConTotalCeroEsCero() {
        assertEquals(0, oa(0, 0).porcentaje)
    }

    @Test
    fun dosDeCuatroEsCincuenta() {
        assertEquals(50, oa(2, 4).porcentaje)
    }

    @Test
    fun etiquetaEnLosLimites() {
        assertEquals("Reforzar", RendimientoOa.etiquetaLogro(49))
        assertEquals("En desarrollo", RendimientoOa.etiquetaLogro(50))
        assertEquals("En desarrollo", RendimientoOa.etiquetaLogro(74))
        assertEquals("Fortaleza", RendimientoOa.etiquetaLogro(75))
    }

    @Test
    fun etiquetaDeUnOa() {
        assertEquals("Fortaleza", oa(2, 2).etiqueta)
        assertEquals("En desarrollo", oa(1, 2).etiqueta)
        assertEquals("Reforzar", oa(0, 2).etiqueta)
    }
}
