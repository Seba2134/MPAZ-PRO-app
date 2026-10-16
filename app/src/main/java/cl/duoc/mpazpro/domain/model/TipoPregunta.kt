package cl.duoc.mpazpro.domain.model

/** Formas de responder, el caso menciona seleccion, ordenar, completar y unir. */
enum class TipoPregunta(val nombreVisible: String) {
    SELECCION("Elegir la respuesta"),
    ORDENAR("Ordenar"),
    COMPLETAR("Completar"),
    UNIR("Unir")
}