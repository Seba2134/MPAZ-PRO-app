package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.DocenteAsignaturaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocenteAsignaturaDao {

    // Para Jefatura UTP: administrar la carga academica

    /** Quién hace cada asignatura en un curso (para la pantalla de UTP). */
    @Query("SELECT * FROM docente_asignatura WHERE cursoId = :cursoId ORDER BY asignaturaId")
    fun obtenerPorCurso(cursoId: Long): Flow<List<DocenteAsignaturaEntity>>

    /**
     * Asigna (o reasigna) un docente a una asignatura de un curso.
     * REPLACE: si la asignatura ya tenía otro profesor, se reemplaza por el nuevo.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun asignar(acceso: DocenteAsignaturaEntity)

    @Delete
    suspend fun quitar(acceso: DocenteAsignaturaEntity)


    /** Toda la carga académica del colegio (para mostrar todos los cursos de una vez). */
    @Query("SELECT * FROM docente_asignatura")
    fun obtenerTodos(): Flow<List<DocenteAsignaturaEntity>>

    /** Deja una asignatura sin profesor de asignatura en ese curso. */
    @Query("DELETE FROM docente_asignatura WHERE cursoId = :cursoId AND asignaturaId = :asignaturaId")
    suspend fun quitarDeAsignatura(cursoId: Long, asignaturaId: Long)

    // ---------- Para el Docente: qué puede ver y hacer ----------

    /**
     * Las asignaturas de un curso a las que el docente tiene acceso:
     * todas si es profesor jefe, o solo las suyas si es profesor de asignatura.
     */
    @Query(
        """
        SELECT a.* FROM asignatura a
        WHERE EXISTS (
                SELECT 1 FROM curso c
                WHERE c.id = :cursoId AND c.profesorJefeId = :docenteId
              )
           OR a.id IN (
                SELECT da.asignaturaId FROM docente_asignatura da
                WHERE da.cursoId = :cursoId AND da.docenteId = :docenteId
              )
        ORDER BY a.id
        """
    )
    fun obtenerAsignaturasPermitidas(docenteId: Long, cursoId: Long): Flow<List<AsignaturaEntity>>

    /** Comprobación puntual antes de una acción: ¿puede este docente actuar sobre esta asignatura? */
    @Query(
        """
        SELECT EXISTS (
            SELECT 1 FROM curso c
            WHERE c.id = :cursoId AND c.profesorJefeId = :docenteId
        ) OR EXISTS (
            SELECT 1 FROM docente_asignatura da
            WHERE da.cursoId = :cursoId AND da.asignaturaId = :asignaturaId AND da.docenteId = :docenteId
        )
        """
    )
    suspend fun tieneAcceso(docenteId: Long, cursoId: Long, asignaturaId: Long): Boolean
}