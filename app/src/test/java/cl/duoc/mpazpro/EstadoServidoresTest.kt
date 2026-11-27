package cl.duoc.mpazpro

import cl.duoc.mpazpro.data.repository.EstadoServidores
import cl.duoc.mpazpro.data.repository.textoEstadoServicio
import org.junit.Assert.assertEquals
import org.junit.Test

class EstadoServidoresTest {

    @Test
    fun textoDeCadaLineaSegunElEstado() {
        assertEquals("Usuarios: conectado ✓", textoEstadoServicio("Usuarios", true))
        assertEquals("Resultados: sin conexión", textoEstadoServicio("Resultados", false))
        assertEquals("Usuarios: revisando…", textoEstadoServicio("Usuarios", null))
    }

    @Test
    fun cadaServicioSeInformaPorSeparado() {
        val estado = EstadoServidores(usuarios = false, resultados = true)

        assertEquals("Usuarios: sin conexión", textoEstadoServicio("Usuarios", estado.usuarios))
        assertEquals("Resultados: conectado ✓", textoEstadoServicio("Resultados", estado.resultados))
    }
}
