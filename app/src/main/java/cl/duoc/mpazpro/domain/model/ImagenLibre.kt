package cl.duoc.mpazpro.domain.model

/**
 * Una imagen libre encontrada en Wikimedia Commons, lista para mostrarse en la búsqueda.
 * [textoLicencia] es el nombre corto que da Commons («CC BY-SA 4.0», «CC0»…);
 * [licencia] es cómo se guarda en la app.
 */
data class ImagenLibre(
    val id: String,
    val titulo: String,
    val urlMiniatura: String,
    val autor: String,
    val textoLicencia: String,
    val licencia: Licencia
)

private const val LARGO_MAXIMO_AUTOR = 80
private const val AUTOR_DESCONOCIDO = "Autor desconocido"

/**
 * Traduce la licencia de Commons a una de la app. Si no se reconoce devuelve null
 * y esa imagen no se muestra (no se puede usar sin saber su licencia).
 */
fun licenciaDesde(textoLicencia: String): Licencia? {
    val texto = textoLicencia.trim()
    return when {
        texto.startsWith("CC0", ignoreCase = true) ||
            texto.contains("Public domain", ignoreCase = true) ||
            texto.contains("PD") -> Licencia.DOMINIO_PUBLICO
        texto.startsWith("CC", ignoreCase = true) -> Licencia.CREATIVE_COMMONS
        else -> null
    }
}

/**
 * Commons suele entregar el autor con HTML (por ejemplo, un enlace a su página).
 * Quita las etiquetas y los espacios sobrantes, decodifica las entidades comunes y lo deja en 80 caracteres.
 */
fun limpiarAutor(html: String): String {
    val texto = limpiarHtml(html).take(LARGO_MAXIMO_AUTOR).trim()
    return texto.ifEmpty { AUTOR_DESCONOCIDO }
}

/** El texto que queda en el campo «autor» del contenido: las licencias CC exigen citar autor y licencia. */
fun textoAutoria(autor: String, textoLicencia: String): String =
    "$autor · $textoLicencia · Wikimedia Commons"

/** El nombre de la imagen: su ObjectName o, si falta, el título del archivo sin «File:». */
fun tituloImagen(objectName: String?, title: String?): String {
    val nombre = objectName?.let { limpiarHtml(it) }.orEmpty()
    if (nombre.isNotEmpty()) return nombre
    return title.orEmpty().removePrefix("File:").trim()
}

/** Quita etiquetas HTML, decodifica &lt; &gt; &quot; &#39; &nbsp; &amp; y junta los espacios. */
internal fun limpiarHtml(html: String): String =
    html.replace(Regex("<[^>]*>"), " ")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")      // al final, para no decodificar dos veces (&amp;quot; → &quot;)
        .replace(Regex("\\s+"), " ")
        .trim()
