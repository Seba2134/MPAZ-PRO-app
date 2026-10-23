package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.local.relation.CursoConEstudiantes
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete

@Dao
interface CursoDao {

    /** Todos los cursos (para Jefatura UTP). */
    @Query("SELECT * FROM curso ORDER BY nombre")
    fun obtenerTodos(): Flow<List<CursoEntity>>

    /**
     * Los cursos que un docente puede ver:
     * donde es profesor jefe, o donde hace al menos una asignatura.
     */
    @Query(
        """
        SELECT * FROM curso
        WHERE profesorJefeId = :docenteId
           OR id IN (SELECT cursoId FROM docente_asignatura WHERE docenteId = :docenteId)
        ORDER BY nombre
        """
    )
    fun obtenerVisiblesParaDocente(docenteId: Long): Flow<List<CursoEntity>>

    /** Un curso con la lista de sus estudiantes (para el monitoreo del docente). */
    @Transaction
    @Query("SELECT * FROM curso WHERE id = :cursoId")
    fun obtenerConEstudiantes(cursoId: Long): Flow<CursoConEstudiantes?>

    @Query("SELECT * FROM curso WHERE id = :id")
    suspend fun obtenerPorId(id: Long): CursoEntity?

    @Insert
    suspend fun insertar(curso: CursoEntity): Long
    /** Todos los cursos con sus estudiantes (para la pantalla de UTP). */
    @Transaction
    @Query("SELECT * FROM curso ORDER BY nombre")
    fun obtenerTodosConEstudiantes(): Flow<List<CursoConEstudiantes>>

    /** ¿Ya hay un curso con ese nombre? (sin importar mayúsculas: "2.º b" = "2.º B") */
    @Query("SELECT EXISTS(SELECT 1 FROM curso WHERE nombre = :nombre COLLATE NOCASE)")
    suspend fun existeNombre(nombre: String): Boolean

    /** Cuenta TODOS los estudiantes del curso, también los desactivados. */
    @Query("SELECT COUNT(*) FROM usuario WHERE cursoId = :cursoId AND rol = 'ESTUDIANTE'")
    suspend fun contarEstudiantes(cursoId: Long): Int

    @Delete
    suspend fun eliminar(curso: CursoEntity)
}