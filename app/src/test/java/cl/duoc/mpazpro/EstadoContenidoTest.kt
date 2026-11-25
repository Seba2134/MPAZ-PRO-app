package cl.duoc.mpazpro

import cl.duoc.mpazpro.domain.model.EstadoContenido.AUTORIZADO
import cl.duoc.mpazpro.domain.model.EstadoContenido.BORRADOR
import cl.duoc.mpazpro.domain.model.EstadoContenido.DESHABILITADO
import cl.duoc.mpazpro.domain.model.EstadoContenido.EN_REVISION
import cl.duoc.mpazpro.domain.model.EstadoContenido.PUBLICADO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadoContenidoTest {

    @Test
    fun transicionesPermitidas() {
        assertTrue(BORRADOR.puedeCambiarA(EN_REVISION))
        assertTrue(EN_REVISION.puedeCambiarA(AUTORIZADO))
        assertTrue(EN_REVISION.puedeCambiarA(BORRADOR))
        assertTrue(AUTORIZADO.puedeCambiarA(PUBLICADO))
        assertTrue(AUTORIZADO.puedeCambiarA(DESHABILITADO))
        assertTrue(PUBLICADO.puedeCambiarA(DESHABILITADO))
        assertTrue(DESHABILITADO.puedeCambiarA(BORRADOR))
    }

    @Test
    fun transicionesProhibidas() {
        assertFalse(BORRADOR.puedeCambiarA(PUBLICADO))
        assertFalse(BORRADOR.puedeCambiarA(AUTORIZADO))
        assertFalse(EN_REVISION.puedeCambiarA(PUBLICADO))
        assertFalse(PUBLICADO.puedeCambiarA(BORRADOR))
        assertFalse(DESHABILITADO.puedeCambiarA(PUBLICADO))
        assertFalse(BORRADOR.puedeCambiarA(BORRADOR))
    }

    @Test
    fun siguientesPermitidosDeBorrador() {
        assertEquals(setOf(EN_REVISION), BORRADOR.siguientesPermitidos())
    }
}
