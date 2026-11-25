package cl.duoc.mpazpro.mpazpro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionFormularioTest {

    private fun esCorreoValido(correo: String): Boolean {
        return correo.isNotBlank() && correo.contains("@") && correo.contains(".")
    }

    private fun esContrasenaValida(password: String): Boolean {
        return password.trim().length >= 6
    }

    @Test
    fun correoValido_retornaTrue() {
        assertTrue(esCorreoValido("estudiante@duocuc.cl"))
    }

    @Test
    fun correoInvalido_retornaFalse() {
        assertFalse(esCorreoValido("correo_sin_arroba"))
        assertFalse(esCorreoValido(""))
    }

    @Test
    fun contrasenaValida_retornaTrue() {
        assertTrue(esContrasenaValida("clave123"))
    }

    @Test
    fun contrasenaCorta_retornaFalse() {
        assertFalse(esContrasenaValida("123"))
    }
}