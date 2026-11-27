package cl.duoc.mpazpro.domain.model


enum class Licencia(val nombreVisible: String) {
    PROPIA("Elaboración propia"),
    MINEDUC("Mineduc"),
    DOMINIO_PUBLICO("Dominio público"),
    CREATIVE_COMMONS("Creative Commons")   // imágenes libres de Wikimedia Commons (CC BY, CC BY-SA…)
}
