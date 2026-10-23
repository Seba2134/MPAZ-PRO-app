package cl.duoc.mpazpro.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.mpazpro.data.seguridad.PasswordHasher

/**
 * Datos de prueba que se cargan la primera vez que se crea la base de datos.
 * Todos los usuarios son ficticios y todo el contenido es de elaboración propia.
 */
object DatosIniciales {

    private const val AUTOR = "Equipo MPAZ PRO"
    private const val UTP_ID = 1L
    private const val PROFESORA_JEFE_ID = 2L
    private const val CURSO_ID = 1L

    fun cargar(db: SupportSQLiteDatabase) {
        cargarAsignaturas(db)
        cargarPersonas(db)
        cargarLenguaje(db)
        cargarMatematica(db)
        cargarCiencias(db)
        cargarHistoria(db)
    }


    // Asignaturas y personas
    private fun cargarAsignaturas(db: SupportSQLiteDatabase) {
        asignatura(db, 1, "Lenguaje", "#E53935")
        asignatura(db, 2, "Matemática", "#1E88E5")
        asignatura(db, 3, "Ciencias Naturales", "#43A047")
        asignatura(db, 4, "Historia", "#FB8C00")
    }

    private fun cargarPersonas(db: SupportSQLiteDatabase) {
        // 1) Jefatura UTP y docentes: sin curso
        usuario(db, 1, "Jefatura UTP", "utp", "utp123", "JEFATURA_UTP", null)
        usuario(db, 2, "Profesora Ana", "ana", "ana123", "DOCENTE", null)
        usuario(db, 3, "Profesor Carlos", "carlos", "carlos123", "DOCENTE", null)
        usuario(db, 4, "Profesora Daniela", "daniela", "daniela123", "DOCENTE", null)

        // 2) El curso, con la Profesora Ana como profesora jefe
        insertar(db, "curso", ContentValues().apply {
            put("id", CURSO_ID)
            put("nombre", "2.º A")
            put("profesorJefeId", PROFESORA_JEFE_ID)
        })

        // 3) Carga académica: Carlos hace Matemática (2) y Daniela hace Ciencias (3)
        docenteAsignatura(db, asignaturaId = 2, docenteId = 3)
        docenteAsignatura(db, asignaturaId = 3, docenteId = 4)

        // 4) Estudiantes del 2.º A
        usuario(db, 5, "Tomás", "tomas", "1234", "ESTUDIANTE", CURSO_ID)
        usuario(db, 6, "Sofía", "sofia", "1234", "ESTUDIANTE", CURSO_ID)
        usuario(db, 7, "Benjamín", "benjamin", "1234", "ESTUDIANTE", CURSO_ID)
        usuario(db, 8, "Isidora", "isidora", "1234", "ESTUDIANTE", CURSO_ID)
    }


    // Lenguaje
    private fun cargarLenguaje(db: SupportSQLiteDatabase) {
        val unidad = unidadPublicadaYAsignada(db, asignaturaId = 1, titulo = "Unidad 1: Leemos cuentos")

        val oa1 = objetivo(db, unidad, "OA 1", "Identificar los personajes y el lugar de un cuento")
        contenido(db, oa1, "TEXTO", 1,
            "Un cuento tiene personajes: son quienes viven la historia. También tiene un lugar, que es donde pasa todo.")
        contenido(db, oa1, "EJEMPLO", 2,
            "En el cuento «El zorro y las uvas», el personaje es el zorro y el lugar es un viñedo.")

        val oa2 = objetivo(db, unidad, "OA 2", "Reconocer el inicio, el desarrollo y el final de un cuento")
        contenido(db, oa2, "TEXTO", 1,
            "Todo cuento tiene tres partes: el inicio (cómo empieza), el desarrollo (qué problema pasa) y el final (cómo termina).")

        val desafio = actividad(db, unidad, "Desafío: ¿Quién es el personaje?",
            "Lee la pregunta y toca la respuesta correcta.", "DESAFIO", 1)
        pregunta(db, desafio, oa1, 1, "En «El zorro y las uvas», ¿quién es el personaje?",
            listOf("Las uvas", "El zorro", "El viñedo"), correcta = 1)
        pregunta(db, desafio, oa2, 2, "¿Cómo se llama la parte donde el cuento termina?",
            listOf("El final", "El inicio", "El desarrollo"), correcta = 0)

        val prueba = actividad(db, unidad, "Prueba final: Leemos cuentos",
            "Responde con calma. ¡Tú puedes!", "PRUEBA_FINAL", 2)
        pregunta(db, prueba, oa1, 1, "¿Qué es un personaje?",
            listOf("El título del cuento", "Quien vive la historia", "El dibujo de la portada"), correcta = 1)
        pregunta(db, prueba, oa1, 2, "Si un cuento pasa en el bosque, ¿cuál es el lugar?",
            listOf("El lobo", "La abuela", "El bosque"), correcta = 2)
        pregunta(db, prueba, oa2, 3, "¿Qué parte del cuento muestra cómo empieza todo?",
            listOf("El inicio", "El final", "El desarrollo"), correcta = 0)
        pregunta(db, prueba, oa2, 4, "¿En qué parte del cuento aparece el problema?",
            listOf("El final", "El desarrollo", "El inicio"), correcta = 1)
    }


    // Matematicas
    private fun cargarMatematica(db: SupportSQLiteDatabase) {
        val unidad = unidadPublicadaYAsignada(db, asignaturaId = 2, titulo = "Unidad 1: Números hasta el 100")

        val oa1 = objetivo(db, unidad, "OA 1", "Contar, leer y comparar números hasta el 100")
        contenido(db, oa1, "TEXTO", 1,
            "Los números se forman con decenas y unidades. Una decena son 10 unidades juntas.")
        contenido(db, oa1, "EJEMPLO", 2,
            "El número 34 tiene 3 decenas y 4 unidades: 10 + 10 + 10 + 4 = 34.")

        val oa2 = objetivo(db, unidad, "OA 2", "Sumar números de dos dígitos sin reserva")
        contenido(db, oa2, "TEXTO", 1,
            "Para sumar, primero juntamos las unidades y después las decenas. Ejemplo: 23 + 14 = 37.")

        val desafio = actividad(db, unidad, "Desafío: Decenas y unidades",
            "Piensa y toca el número correcto.", "DESAFIO", 1)
        pregunta(db, desafio, oa1, 1, "¿Cuántas decenas tiene el número 52?",
            listOf("2", "52", "5"), correcta = 2)
        pregunta(db, desafio, oa2, 2, "¿Cuánto es 10 + 20?",
            listOf("30", "12", "40"), correcta = 0)

        val prueba = actividad(db, unidad, "Prueba final: Números hasta el 100",
            "Responde con calma. ¡Tú puedes!", "PRUEBA_FINAL", 2)
        pregunta(db, prueba, oa1, 1, "¿Qué número viene después del 49?",
            listOf("48", "50", "59"), correcta = 1)
        pregunta(db, prueba, oa1, 2, "¿Cuál número es el mayor?",
            listOf("17", "70", "71"), correcta = 2)
        pregunta(db, prueba, oa2, 3, "¿Cuánto es 21 + 15?",
            listOf("36", "35", "46"), correcta = 0)
        pregunta(db, prueba, oa2, 4, "¿Cuánto es 40 + 30?",
            listOf("60", "70", "43"), correcta = 1)
    }

    // Ciencias Naturales
    private fun cargarCiencias(db: SupportSQLiteDatabase) {
        val unidad = unidadPublicadaYAsignada(db, asignaturaId = 3, titulo = "Unidad 1: Los animales")

        val oa1 = objetivo(db, unidad, "OA 1", "Clasificar animales según su cubierta corporal: pelos, plumas o escamas")
        contenido(db, oa1, "TEXTO", 1,
            "Los animales tienen distintas cubiertas en su cuerpo: algunos tienen pelos, otros plumas y otros escamas.")
        contenido(db, oa1, "EJEMPLO", 2,
            "El perro tiene pelos, la gallina tiene plumas y el pez tiene escamas.")

        val oa2 = objetivo(db, unidad, "OA 2", "Reconocer cómo se desplazan los animales")
        contenido(db, oa2, "TEXTO", 1,
            "Los animales se mueven de distintas formas: caminan, vuelan, nadan o se arrastran.")

        val desafio = actividad(db, unidad, "Desafío: ¿Qué animal es?",
            "Lee y toca la respuesta correcta.", "DESAFIO", 1)
        pregunta(db, desafio, oa1, 1, "¿Qué animal tiene plumas?",
            listOf("El perro", "La gallina", "El pez"), correcta = 1)
        pregunta(db, desafio, oa2, 2, "¿Cómo se mueve un pez?",
            listOf("Nada", "Vuela", "Camina"), correcta = 0)

        val prueba = actividad(db, unidad, "Prueba final: Los animales",
            "Responde con calma. ¡Tú puedes!", "PRUEBA_FINAL", 2)
        pregunta(db, prueba, oa1, 1, "¿Qué cubierta tiene un gato?",
            listOf("Plumas", "Escamas", "Pelos"), correcta = 2)
        pregunta(db, prueba, oa1, 2, "¿Qué animal tiene escamas?",
            listOf("La serpiente", "La oveja", "El loro"), correcta = 0)
        pregunta(db, prueba, oa2, 3, "¿Qué animal vuela?",
            listOf("El caballo", "El cóndor", "La tortuga"), correcta = 1)
        pregunta(db, prueba, oa2, 4, "¿Cómo se mueve una culebra?",
            listOf("Vuela", "Salta", "Se arrastra"), correcta = 2)
    }


    // Historia
    private fun cargarHistoria(db: SupportSQLiteDatabase) {
        val unidad = unidadPublicadaYAsignada(db, asignaturaId = 4, titulo = "Unidad 1: Mi país, Chile")

        val oa1 = objetivo(db, unidad, "OA 1", "Reconocer los símbolos patrios de Chile")
        contenido(db, oa1, "TEXTO", 1,
            "Los símbolos patrios de Chile son la bandera, el escudo y el himno nacional.")
        contenido(db, oa1, "EJEMPLO", 2,
            "La bandera de Chile tiene una estrella blanca sobre un cuadro azul, una franja blanca y una franja roja.")

        val oa2 = objetivo(db, unidad, "OA 2", "Ubicar Chile en un mapa y reconocer su forma")
        contenido(db, oa2, "TEXTO", 1,
            "Chile es un país largo y angosto. Al este está la cordillera de los Andes y al oeste el océano Pacífico.")

        val desafio = actividad(db, unidad, "Desafío: Conozco mi país",
            "Lee y toca la respuesta correcta.", "DESAFIO", 1)
        pregunta(db, desafio, oa1, 1, "¿Cuál de estos es un símbolo patrio?",
            listOf("Una pelota", "Un árbol", "La bandera"), correcta = 2)
        pregunta(db, desafio, oa2, 2, "¿Qué océano está junto a Chile?",
            listOf("El Pacífico", "El Atlántico", "El Índico"), correcta = 0)

        val prueba = actividad(db, unidad, "Prueba final: Mi país, Chile",
            "Responde con calma. ¡Tú puedes!", "PRUEBA_FINAL", 2)
        pregunta(db, prueba, oa1, 1, "¿De qué color es la estrella de la bandera de Chile?",
            listOf("Roja", "Blanca", "Azul"), correcta = 1)
        pregunta(db, prueba, oa1, 2, "¿Qué canción es un símbolo patrio?",
            listOf("El himno nacional", "Una canción de cuna", "Un villancico"), correcta = 0)
        pregunta(db, prueba, oa2, 3, "¿Qué cordillera está en Chile?",
            listOf("Los Alpes", "Los Pirineos", "Los Andes"), correcta = 2)
        pregunta(db, prueba, oa2, 4, "¿Cómo es la forma de Chile?",
            listOf("Redonda", "Larga y angosta", "Cuadrada"), correcta = 1)
    }


    // Ayudantes: cada uno inserta una fila y devuelve su id
    private fun insertar(db: SupportSQLiteDatabase, tabla: String, valores: ContentValues): Long =
        db.insert(tabla, SQLiteDatabase.CONFLICT_ABORT, valores)

    private fun asignatura(db: SupportSQLiteDatabase, id: Long, nombre: String, color: String) {
        insertar(db, "asignatura", ContentValues().apply {
            put("id", id)
            put("nombre", nombre)
            put("colorHex", color)
        })
    }

    private fun usuario(
        db: SupportSQLiteDatabase,
        id: Long,
        nombre: String,
        nombreUsuario: String,
        password: String,
        rol: String,
        cursoId: Long?
    ) {
        val credencial = PasswordHasher.generar(password)
        insertar(db, "usuario", ContentValues().apply {
            put("id", id)
            put("run", "")
            put("nombre", nombre)
            put("correo", "$nombreUsuario@mpazpro.cl")      // correo ficticio
            put("nombreUsuario", nombreUsuario)
            put("passwordHash", credencial.hash)
            put("passwordSal", credencial.sal)
            put("rol", rol)
            if (cursoId == null) putNull("cursoId") else put("cursoId", cursoId)
            put("activo", 1)
        })
    }

    private fun docenteAsignatura(db: SupportSQLiteDatabase, asignaturaId: Long, docenteId: Long) {
        insertar(db, "docente_asignatura", ContentValues().apply {
            put("cursoId", CURSO_ID)
            put("asignaturaId", asignaturaId)
            put("docenteId", docenteId)
        })
    }

    /**
     * Crea una unidad ya Publicada, registra ese estado en el historial
     * y la asigna al 2.º A (2 intentos para la prueba, sin fechas límite).
     */
    private fun unidadPublicadaYAsignada(db: SupportSQLiteDatabase, asignaturaId: Long, titulo: String): Long {
        val ahora = System.currentTimeMillis()

        val unidadId = insertar(db, "unidad", ContentValues().apply {
            put("asignaturaId", asignaturaId)
            put("titulo", titulo)
            put("orden", 1)
            put("estado", "PUBLICADO")
        })

        insertar(db, "historial_cambio", ContentValues().apply {
            put("unidadId", unidadId)
            putNull("estadoAnterior")
            put("estadoNuevo", "PUBLICADO")
            put("usuarioId", UTP_ID)
            put("comentario", "Cargada con los datos iniciales")
            put("fecha", ahora)
        })

        insertar(db, "asignacion", ContentValues().apply {
            put("cursoId", CURSO_ID)
            put("unidadId", unidadId)
            put("asignadoPorId", PROFESORA_JEFE_ID)
            put("fechaAsignacion", ahora)
            put("instrucciones", "Lee con calma cada pregunta. ¡Tú puedes!")
            put("intentosMaxPrueba", 2)
            putNull("fechaInicioPrueba")
            putNull("fechaFinPrueba")
            put("mostrarRespuestas", 1)
        })

        return unidadId
    }

    private fun objetivo(db: SupportSQLiteDatabase, unidadId: Long, codigo: String, descripcion: String): Long =
        insertar(db, "objetivo_aprendizaje", ContentValues().apply {
            put("unidadId", unidadId)
            put("codigo", codigo)
            put("descripcion", descripcion)
        })

    private fun contenido(db: SupportSQLiteDatabase, oaId: Long, tipo: String, orden: Int, texto: String) {
        insertar(db, "contenido", ContentValues().apply {
            put("oaId", oaId)
            put("tipo", tipo)
            put("texto", texto)
            putNull("recurso")
            put("orden", orden)
            put("autor", AUTOR)
            put("licencia", "PROPIA")
        })
    }

    private fun actividad(
        db: SupportSQLiteDatabase,
        unidadId: Long,
        titulo: String,
        instruccion: String,
        tipo: String,
        orden: Int
    ): Long = insertar(db, "actividad", ContentValues().apply {
        put("unidadId", unidadId)
        put("titulo", titulo)
        put("instruccion", instruccion)
        put("tipo", tipo)
        put("orden", orden)
    })

    /** Crea una pregunta de selección con sus opciones. [correcta] es la posición (0, 1 o 2) de la opción correcta. */
    private fun pregunta(
        db: SupportSQLiteDatabase,
        actividadId: Long,
        oaId: Long,
        orden: Int,
        enunciado: String,
        opciones: List<String>,
        correcta: Int
    ) {
        val preguntaId = insertar(db, "pregunta", ContentValues().apply {
            put("actividadId", actividadId)
            put("oaId", oaId)
            put("enunciado", enunciado)
            put("tipo", "SELECCION")
            putNull("recursoAudio")
            putNull("recursoImagen")
            put("orden", orden)
        })

        opciones.forEachIndexed { indice, texto ->
            insertar(db, "opcion", ContentValues().apply {
                put("preguntaId", preguntaId)
                put("texto", texto)
                putNull("recursoImagen")
                put("esCorrecta", if (indice == correcta) 1 else 0)
                put("orden", indice + 1)
            })
        }
    }
}