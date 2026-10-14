package cl.duoc.mpazpro.domain.model

enum class EstadoContenido(val nombreVisible: String) {
    BORRADOR("Borrador"),
    EN_REVISION("En revisión"),
    AUTORIZADO("Autorizado"),
    PUBLICADO("Publicado"),
    DESHABILITADO("Deshabilitado")
}