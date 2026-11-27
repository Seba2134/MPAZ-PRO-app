package cl.duoc.mpazpro

import cl.duoc.mpazpro.data.remote.dto.RespuestaWikimediaDto
import cl.duoc.mpazpro.data.repository.imagenesDesde
import cl.duoc.mpazpro.domain.model.Licencia
import cl.duoc.mpazpro.domain.model.licenciaDesde
import cl.duoc.mpazpro.domain.model.limpiarAutor
import cl.duoc.mpazpro.domain.model.textoAutoria
import cl.duoc.mpazpro.domain.validacion.Validaciones
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Licencias, autoría y lectura de la respuesta de Wikimedia Commons (en la JVM, sin red). */
class WikimediaMapeoTest {

    @Test
    fun licenciaDesdeElTextoDeCommons() {
        assertEquals(Licencia.CREATIVE_COMMONS, licenciaDesde("CC BY-SA 4.0"))
        assertEquals(Licencia.CREATIVE_COMMONS, licenciaDesde("CC BY 2.0"))
        assertEquals(Licencia.DOMINIO_PUBLICO, licenciaDesde("CC0"))
        assertEquals(Licencia.DOMINIO_PUBLICO, licenciaDesde("Public domain"))
        assertEquals(Licencia.DOMINIO_PUBLICO, licenciaDesde("PD-old"))
        assertNull(licenciaDesde("GFDL"))
        assertNull(licenciaDesde(""))
    }

    @Test
    fun limpiarAutorQuitaHtmlYDecodificaEntidades() {
        assertEquals(
            "Juan Pérez",
            limpiarAutor("<a href=\"//commons.wikimedia.org/wiki/User:Juan\" title=\"User:Juan\">Juan  Pérez</a>")
        )
        assertEquals("Ana & \"Equipo\" O'Hara", limpiarAutor("Ana &amp; &quot;Equipo&quot; O&#39;Hara"))
        assertEquals("Autor desconocido", limpiarAutor(""))
        assertEquals("Autor desconocido", limpiarAutor("<span></span>  "))
        assertEquals(80, limpiarAutor("a".repeat(200)).length)
    }

    @Test
    fun textoDeAutoriaCompleto() {
        assertEquals(
            "Juan Pérez · CC BY-SA 4.0 · Wikimedia Commons",
            textoAutoria("Juan Pérez", "CC BY-SA 4.0")
        )
    }

    @Test
    fun laRespuestaSeOrdenaYFiltra() {
        val json = """
            {"batchcomplete":true,"query":{"pages":[
              {"pageid":3,"index":3,"title":"File:Mariposa azul.png","imageinfo":[{"thumburl":"https://x/3.png","mime":"image/png",
                "extmetadata":{"Artist":{"value":"<b>Luis</b>"},"LicenseShortName":{"value":"CC0"}}}]},
              {"pageid":1,"index":1,"title":"File:Mariposa monarca.jpg","imageinfo":[{"thumburl":"https://x/1.jpg","mime":"image/jpeg",
                "extmetadata":{"Artist":{"value":"Ana"},"LicenseShortName":{"value":"CC BY-SA 4.0"},"ObjectName":{"value":"Mariposa monarca"}}}]},
              {"pageid":2,"index":2,"title":"File:Mariposa volando.webm","imageinfo":[{"thumburl":"https://x/2.jpg","mime":"video/webm",
                "extmetadata":{"Artist":{"value":"Pedro"},"LicenseShortName":{"value":"CC BY 3.0"}}}]},
              {"pageid":4,"index":4,"title":"File:Mariposa rara.jpg","imageinfo":[{"thumburl":"https://x/4.jpg","mime":"image/jpeg",
                "extmetadata":{"Artist":{"value":"Eva"},"LicenseShortName":{"value":"Licencia desconocida"}}}]}
            ]}}
        """.trimIndent()

        val imagenes = imagenesDesde(Gson().fromJson(json, RespuestaWikimediaDto::class.java))

        // El video y la licencia desconocida se descartan; el resto queda en el orden de la búsqueda
        assertEquals(listOf("1", "3"), imagenes.map { it.id })
        val primera = imagenes.first()
        assertEquals("Mariposa monarca", primera.titulo)
        assertEquals("Ana", primera.autor)
        assertEquals("CC BY-SA 4.0", primera.textoLicencia)
        assertEquals(Licencia.CREATIVE_COMMONS, primera.licencia)
        assertEquals("https://x/1.jpg", primera.urlMiniatura)
        // Sin ObjectName se usa el título del archivo sin «File:»; el autor queda sin HTML
        assertEquals("Mariposa azul.png", imagenes[1].titulo)
        assertEquals("Luis", imagenes[1].autor)
        assertEquals(Licencia.DOMINIO_PUBLICO, imagenes[1].licencia)
    }

    @Test
    fun respuestaSinQueryEsListaVacia() {
        val imagenes = imagenesDesde(Gson().fromJson("{\"batchcomplete\":true}", RespuestaWikimediaDto::class.java))
        assertTrue(imagenes.isEmpty())
    }

    @Test
    fun busquedaEntreDosYCincuentaCaracteres() {
        assertNull(Validaciones.busquedaImagen("mariposa"))
        assertNotNull(Validaciones.busquedaImagen("m"))
        assertNotNull(Validaciones.busquedaImagen("a".repeat(51)))
    }
}
