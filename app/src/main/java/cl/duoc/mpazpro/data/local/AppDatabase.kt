package cl.duoc.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.mpazpro.data.local.dao.ActividadDao
import cl.duoc.mpazpro.data.local.dao.AsignacionDao
import cl.duoc.mpazpro.data.local.dao.AsignaturaDao
import cl.duoc.mpazpro.data.local.dao.ContenidoDao
import cl.duoc.mpazpro.data.local.dao.CursoDao
import cl.duoc.mpazpro.data.local.dao.DocenteAsignaturaDao
import cl.duoc.mpazpro.data.local.dao.HistorialCambioDao
import cl.duoc.mpazpro.data.local.dao.IntentoDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoAprendizajeDao
import cl.duoc.mpazpro.data.local.dao.ObservacionDocenteDao
import cl.duoc.mpazpro.data.local.dao.OpcionDao
import cl.duoc.mpazpro.data.local.dao.PreguntaDao
import cl.duoc.mpazpro.data.local.dao.RespuestaDao
import cl.duoc.mpazpro.data.local.dao.UnidadDao
import cl.duoc.mpazpro.data.local.dao.UsuarioDao
import cl.duoc.mpazpro.data.local.entity.ActividadEntity
import cl.duoc.mpazpro.data.local.entity.AsignacionEntity
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.local.entity.DocenteAsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.HistorialCambioEntity
import cl.duoc.mpazpro.data.local.entity.IntentoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entity.ObservacionDocenteEntity
import cl.duoc.mpazpro.data.local.entity.OpcionEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
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
        // Bloque 2: personas y acceso
        UsuarioEntity::class,
        CursoEntity::class,
        DocenteAsignaturaEntity::class,
        // Bloque 3: asignación del docente
        AsignacionEntity::class,
        // Bloque 4: resultados y registro
        IntentoEntity::class,
        RespuestaEntity::class,
        ObservacionDocenteEntity::class,
        HistorialCambioEntity::class
    ],
    version = 12,
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
    abstract fun docenteAsignaturaDao(): DocenteAsignaturaDao

    // Bloque 3
    abstract fun asignacionDao(): AsignacionDao

    // Bloque 4
    abstract fun intentoDao(): IntentoDao
    abstract fun respuestaDao(): RespuestaDao
    abstract fun observacionDocenteDao(): ObservacionDocenteDao
    abstract fun historialCambioDao(): HistorialCambioDao

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
         * Carga los datos iniciales al abrir la base de datos, solo si está vacía.
         * Funciona en la primera instalación y también después de subir la versión
         * (cuando Room borra y recrea las tablas).
         */
        private val datosIniciales = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                if (estaVacia(db)) {
                    insertarDatosIniciales(db)
                }
            }
        }

        private fun estaVacia(db: SupportSQLiteDatabase): Boolean {
            db.query("SELECT COUNT(*) FROM asignatura").use { cursor ->
                return cursor.moveToFirst() && cursor.getInt(0) == 0
            }
        }
        private fun insertarDatosIniciales(db: SupportSQLiteDatabase) {
            db.beginTransaction()
            try {
                DatosIniciales.cargar(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}