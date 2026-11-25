package cl.duoc.mpazpro

import cl.duoc.mpazpro.data.seguridad.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun verificarAceptaLaClaveCorrectaYRechazaOtra() {
        val credencial = PasswordHasher.generar("ana123")

        assertTrue(PasswordHasher.verificar("ana123", credencial.hash, credencial.sal))
        assertFalse(PasswordHasher.verificar("ana124", credencial.hash, credencial.sal))
        assertFalse(PasswordHasher.verificar("", credencial.hash, credencial.sal))
    }

    @Test
    fun dosHashesDeLaMismaClaveTienenSalYHashDistintos() {
        val primera = PasswordHasher.generar("1234")
        val segunda = PasswordHasher.generar("1234")

        assertNotEquals(primera.sal, segunda.sal)
        assertNotEquals(primera.hash, segunda.hash)
        // Y aun así las dos verifican la misma clave
        assertTrue(PasswordHasher.verificar("1234", segunda.hash, segunda.sal))
    }

    @Test
    fun nuncaGuardaLaClaveEnTextoPlano() {
        val credencial = PasswordHasher.generar("utp123")
        assertFalse(credencial.hash.contains("utp123"))
    }
}
