package cl.duoc.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.mpazpro.data.local.dao.ActividadDao
import cl.duoc.mpazpro.data.local.dao.AsignaturaDao
import cl.duoc.mpazpro.data.local.dao.ContenidoDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoAprendizajeDao
import cl.duoc.mpazpro.data.local.dao.OpcionDao
import cl.duoc.mpazpro.data.local.dao.PreguntaDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity

//Si se agrega o cambia una entidad, hay que subir la "version" en 1, sino salta un error
//y la app no compila
@Database(
    entities = [
        // Bloque 1: contenido pedagógico
        AsignaturaEntity::class,
        UnidadEntity::class,
        ObjetivoAprendizajeEntity::class,
        ContenidoEntity::class,
        ActividadEntity::class,
        PreguntaEntity::class,
        OpcionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun asignaturaDao(): AsignaturaDao
    abstract fun unidadDao(): UnidadDao
    abstract fun objetivoAprendizajeDao(): ObjetivoAprendizajeDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun preguntaDao(): PreguntaDao
    abstract fun opcionDao(): OpcionDao

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

        /** Se ejecuta solo la primera vez que se crea la base de datos. */
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
            db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Lenguaje', '#E53935')")
            db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Matemática', '#1E88E5')")
            db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Ciencias Naturales', '#43A047')")
            db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Historia', '#FB8C00')")
        }
    }
}