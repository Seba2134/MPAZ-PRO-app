package cl.duoc.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.mpazpro.data.local.dao.ActividadDao
import cl.duoc.mpazpro.data.local.dao.AsignaturaDao
import cl.duoc.mpazpro.data.local.dao.ContenidoDao
import cl.duoc.mpazpro.data.local.dao.CursoDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoAprendizajeDao
import cl.duoc.mpazpro.data.local.dao.OpcionDao
import cl.duoc.mpazpro.data.local.dao.PreguntaDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.dao.UsuarioDao
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity

@Database(
    entities = [
        // Bloque 1: contenido pedagógico
        AsignaturaEntity::class,
        UnidadEntity::class,
        ObjetivoAprendizajeEntity::class,
        ContenidoEntity::class,
        ActividadEntity::class,
        PreguntaEntity::class,
        OpcionEntity::class,
        // Bloque 2: personas
        UsuarioEntity::class,
        CursoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Bloque 1
    abstract fun asignaturaDao(): AsignaturaDao
    abstract fun unidadDao(): UnidadDao
    abstract fun objetivoAprendizajeDao(): ObjetivoAprendizajeDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun preguntaDao(): PreguntaDao
    abstract fun opcionDao(): OpcionDao

    // Bloque 2
    abstract fun usuarioDao(): UsuarioDao
    abstract fun cursoDao(): CursoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpazpro.db"
                )
                    .addCallback(datosIniciales)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }

        /**
         * Carga los datos iniciales cada vez que las tablas se crean desde cero:
         * - onCreate: la primera vez que se instala la app.
         * - onDestructiveMigration: cuando subimos la versión y Room recrea las tablas.
         */
        private val datosIniciales = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertarDatosIniciales(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                insertarDatosIniciales(db)
            }
        }

        private fun insertarDatosIniciales(db: SupportSQLiteDatabase) {
            // ---------- Asignaturas ----------
            db.execSQL("INSERT INTO asignatura (id, nombre, colorHex) VALUES (1, 'Lenguaje', '#E53935')")
            db.execSQL("INSERT INTO asignatura (id, nombre, colorHex) VALUES (2, 'Matemática', '#1E88E5')")
            db.execSQL("INSERT INTO asignatura (id, nombre, colorHex) VALUES (3, 'Ciencias Naturales', '#43A047')")
            db.execSQL("INSERT INTO asignatura (id, nombre, colorHex) VALUES (4, 'Historia', '#FB8C00')")

            // ---------- Usuarios ficticios ----------
            // 1) Jefatura UTP y docente: sin curso
            db.execSQL("INSERT INTO usuario (id, nombre, rol, cursoId) VALUES (1, 'Jefatura UTP', 'JEFATURA_UTP', NULL)")
            db.execSQL("INSERT INTO usuario (id, nombre, rol, cursoId) VALUES (2, 'Profesora Ana', 'DOCENTE', NULL)")

            // 2) El curso, a cargo de la docente (id 2)
            db.execSQL("INSERT INTO curso (id, nombre, docenteId) VALUES (1, '2.º A', 2)")

            // 3) Los estudiantes, en el curso 1
            db.execSQL("INSERT INTO usuario (id, nombre, rol, cursoId) VALUES (3, 'Tomás', 'ESTUDIANTE', 1)")
            db.execSQL("INSERT INTO usuario (id, nombre, rol, cursoId) VALUES (4, 'Sofía', 'ESTUDIANTE', 1)")
            db.execSQL("INSERT INTO usuario (id, nombre, rol, cursoId) VALUES (5, 'Benjamín', 'ESTUDIANTE', 1)")
            db.execSQL("INSERT INTO usuario (id, nombre, rol, cursoId) VALUES (6, 'Isidora', 'ESTUDIANTE', 1)")
        }
    }
}