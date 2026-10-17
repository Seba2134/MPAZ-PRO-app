package cl.duoc.mpazpro.domain.model


enum class EstadoContenido(val nombreVisible: String) {
    BORRADOR("Borrador"),           // UTP lo está creando, nadie mas lo ve
    EN_REVISION("En revisión"),     // listo para que lo revisen
    AUTORIZADO("Autorizado"),       // aprobado: el docente ya puede verlo y asignarlo
    PUBLICADO("Publicado"),         // visible para los estudiantes
    DESHABILITADO("Deshabilitado"); // retirado: ya no se muestra a nadie

    /** ¿Se puede pasar de este estado al estado [nuevo]? */
    fun puedeCambiarA(nuevo: EstadoContenido): Boolean = nuevo in siguientesPermitidos()

    /** Los estados a los que se puede pasar desde el estado actual. */
    fun siguientesPermitidos(): Set<EstadoContenido> = when (this) {
        BORRADOR -> setOf(EN_REVISION)
        EN_REVISION -> setOf(AUTORIZADO, BORRADOR)
        AUTORIZADO -> setOf(PUBLICADO, BORRADOR, DESHABILITADO)
        PUBLICADO -> setOf(DESHABILITADO)
        DESHABILITADO -> setOf(BORRADOR)
    }
}