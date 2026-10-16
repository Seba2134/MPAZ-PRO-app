package cl.duoc.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.mpazpro.data.local.dao.AsignaturaDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoAprendizajeDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entity.UnidadEntity

@Database(
    entities = [
        AsignaturaEntity::class,
        UnidadEntity::class,
        ObjetivoAprendizajeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun asignaturaDao(): AsignaturaDao
    abstract fun unidadDao(): UnidadDao
    abstract fun objetivoAprendizajeDao(): ObjetivoAprendizajeDao

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
        private val datosIniciales = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Lenguaje', '#E53935')")
                db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Matemática', '#1E88E5')")
                db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Ciencias Naturales', '#43A047')")
                db.execSQL("INSERT INTO asignatura (nombre, colorHex) VALUES ('Historia', '#FB8C00')")
            }
        }
    }
}