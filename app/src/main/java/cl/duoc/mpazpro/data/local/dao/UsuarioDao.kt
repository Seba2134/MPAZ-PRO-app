package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.domain.model.Rol
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    /** Para el login: busca la cuenta por su nombre de usuario. */
    @Query("SELECT * FROM usuario WHERE nombreUsuario = :nombreUsuario LIMIT 1")
    suspend fun buscarPorNombreUsuario(nombreUsuario: String): UsuarioEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM usuario WHERE nombreUsuario = :nombreUsuario)")
    suspend fun existeNombreUsuario(nombreUsuario: String): Boolean

    /** Para Jefatura UTP: todas las cuentas. */
    @Query("SELECT * FROM usuario ORDER BY rol, nombre")
    fun obtenerTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuario WHERE rol = :rol ORDER BY nombre")
    fun obtenerPorRol(rol: Rol): Flow<List<UsuarioEntity>>

    /** Para el docente: los estudiantes activos de su curso. */
    @Query("SELECT * FROM usuario WHERE cursoId = :cursoId AND rol = 'ESTUDIANTE' AND activo = 1 ORDER BY nombre")
    fun obtenerEstudiantesDeCurso(cursoId: Long): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)
}