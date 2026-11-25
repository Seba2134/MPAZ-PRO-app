package cl.duoc.mpazpro.ui.navigation

object Rutas {
    const val LOGIN = "login"

    const val UTP_INICIO = "utp/inicio"
    const val UTP_CREAR_USUARIO = "utp/crear-usuario"
    const val UTP_CREAR_CONTENIDO = "utp/crear-contenido"
    const val UTP_CURSOS = "utp/cursos"
    const val UTP_UNIDADES = "utp/unidades"
    const val UTP_UNIDAD = "utp/unidad/{unidadId}"
    const val UTP_ACTIVIDAD = "utp/actividad/{actividadId}"

    const val DOCENTE_INICIO = "docente/inicio"
    const val DOCENTE_CONFIGURAR_PRUEBA = "docente/prueba/{asignacionId}"
    const val DOCENTE_ASIGNAR = "docente/asignar"
    const val DOCENTE_AVANCE = "docente/avance/{asignacionId}"
    const val DOCENTE_INFORME = "docente/informe/{asignacionId}/{estudianteId}"

    const val ESTUDIANTE_INICIO = "estudiante/inicio"
    const val ESTUDIANTE_UNIDAD = "estudiante/unidad/{unidadId}"
    const val ESTUDIANTE_ACTIVIDAD = "estudiante/actividad/{actividadId}"

    /** Arma la ruta real, por ejemplo: "docente/prueba/3". */
    fun configurarPrueba(asignacionId: Long): String = "docente/prueba/$asignacionId"

    fun avance(asignacionId: Long): String = "docente/avance/$asignacionId"

    fun informe(asignacionId: Long, estudianteId: Long): String = "docente/informe/$asignacionId/$estudianteId"

    /** Ejemplo: "utp/unidad/2". */
    fun unidadUtp(unidadId: Long): String = "utp/unidad/$unidadId"

    /** Ejemplo: "utp/actividad/5". */
    fun actividadUtp(actividadId: Long): String = "utp/actividad/$actividadId"

    /** Ejemplo: "estudiante/unidad/2". */
    fun unidad(unidadId: Long): String = "estudiante/unidad/$unidadId"

    /** Ejemplo: "estudiante/actividad/5". */
    fun actividad(actividadId: Long): String = "estudiante/actividad/$actividadId"
}
