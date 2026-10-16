package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.domain.model.Rol
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    /** Para la pantalla "de seleccionar rol */
    @Query("SELECT * FROM usuario WHERE rol = :rol ORDER BY nombre")
    fun obtenerPorRol(rol: Rol): Flow<List<UsuarioEntity>>

    /** Para el docente: los estudiantes de su curso. */
    @Query("SELECT * FROM usuario WHERE cursoId = :cursoId AND rol = 'ESTUDIANTE' ORDER BY nombre")
    fun obtenerEstudiantesDeCurso(cursoId: Long): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long
}