package cl.duoc.mpazpro.ui.navigation

object Rutas {
    const val LOGIN = "login"

    const val UTP_INICIO = "utp/inicio"
    const val UTP_CREAR_USUARIO = "utp/crear-usuario"

    const val DOCENTE_INICIO = "docente/inicio"
    const val DOCENTE_CONFIGURAR_PRUEBA = "docente/prueba/{asignacionId}"

    const val ESTUDIANTE_INICIO = "estudiante/inicio"

    /** Arma la ruta real, por ejemplo: "docente/prueba/3". */
    fun configurarPrueba(asignacionId: Long): String = "docente/prueba/$asignacionId"
}