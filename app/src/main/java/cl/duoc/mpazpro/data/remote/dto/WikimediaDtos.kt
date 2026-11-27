package cl.duoc.mpazpro.data.remote.dto

import com.google.gson.annotations.SerializedName

// La respuesta de la búsqueda en la API de MediaWiki de Commons (formatversion=2).
// Todo es nullable: si falta algo, la imagen simplemente se descarta.

/** Si no hay resultados, la respuesta no trae «query». */
data class RespuestaWikimediaDto(
    val query: ConsultaWikimediaDto? = null
)

data class ConsultaWikimediaDto(
    val pages: List<PaginaWikimediaDto>? = null
)

data class PaginaWikimediaDto(
    val pageid: Long? = null,
    val index: Int? = null,               // el orden del resultado en la búsqueda
    val title: String? = null,            // «File:Mariposa monarca.jpg»
    val imageinfo: List<InfoImagenDto>? = null
)

data class InfoImagenDto(
    val thumburl: String? = null,         // la imagen reducida a 640 px (PNG o JPG, también para SVG)
    val mime: String? = null,
    val extmetadata: MetadatosImagenDto? = null
)

data class MetadatosImagenDto(
    @SerializedName("Artist") val artist: ValorMetadatoDto? = null,
    @SerializedName("LicenseShortName") val licenseShortName: ValorMetadatoDto? = null,
    @SerializedName("ObjectName") val objectName: ValorMetadatoDto? = null
)

data class ValorMetadatoDto(
    val value: String? = null
)
