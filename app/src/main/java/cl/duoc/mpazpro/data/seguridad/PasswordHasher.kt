package cl.duoc.mpazpro.data.seguridad

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Genera y verifica hashes de contraseñas con PBKDF2-HMAC-SHA256 y sal aleatoria.
 * Las contraseñas NUNCA se guardan: solo su hash y su sal.
 */
object PasswordHasher {

    private const val ALGORITMO = "PBKDF2WithHmacSHA256"
    private const val ITERACIONES = 120_000
    private const val LARGO_HASH_BITS = 256
    private const val LARGO_SAL_BYTES = 16

    /** El hash y la sal de una contraseña, en texto (Base64) para guardarlos en la base de datos. */
    data class Credencial(val hash: String, val sal: String)

    /** Crea el hash de una contraseña nueva, con una sal aleatoria nueva. */
    fun generar(password: String): Credencial {
        val sal = ByteArray(LARGO_SAL_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = calcular(password, sal)
        return Credencial(hash = codificar(hash), sal = codificar(sal))
    }

    /** ¿La contraseña escrita corresponde al hash guardado? */
    fun verificar(password: String, hashGuardado: String, salGuardada: String): Boolean {
        val sal = decodificar(salGuardada)
        val esperado = decodificar(hashGuardado)
        val calculado = calcular(password, sal)
        return MessageDigest.isEqual(esperado, calculado)
    }

    private fun calcular(password: String, sal: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), sal, ITERACIONES, LARGO_HASH_BITS)
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun codificar(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decodificar(texto: String): ByteArray = Base64.getDecoder().decode(texto)
}