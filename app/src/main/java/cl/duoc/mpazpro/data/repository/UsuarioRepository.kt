package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.dao.CursoDao
import cl.duoc.mpazpro.data.local.dao.DocenteAsignaturaDao
import cl.duoc.mpazpro.data.local.dao.UsuarioDao
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.local.entity.DocenteAsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.data.local.relation.CursoConEstudiantes
import cl.duoc.mpazpro.domain.model.Rol
import kotlinx.coroutines.flow.Flow

/** Usuarios, cursos, carga académica y permisos de los docentes. */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val cursoDao: CursoDao,
    private val docenteAsignaturaDao: DocenteAsignaturaDao
) {

    // ---------- Perfiles ----------

    /** Para la pantalla "¿Quién eres?": los perfiles de un rol. */
    fun perfilesPorRol(rol: Rol): Flow<List<UsuarioEntity>> = usuarioDao.obtenerPorRol(rol)

    suspend fun usuario(id: Long): UsuarioEntity? = usuarioDao.obtenerPorId(id)

    // ---------- Cursos ----------

    fun todosLosCursos(): Flow<List<CursoEntity>> = cursoDao.obtenerTodos()

    /** Los cursos donde el docente es profesor jefe o hace alguna asignatura. */
    fun cursosDelDocente(docenteId: Long): Flow<List<CursoEntity>> =
        cursoDao.obtenerVisiblesParaDocente(docenteId)

    fun cursoConEstudiantes(cursoId: Long): Flow<CursoConEstudiantes?> = cursoDao.obtenerConEstudiantes(cursoId)

    suspend fun curso(cursoId: Long): CursoEntity? = cursoDao.obtenerPorId(cursoId)

    // ---------- Permisos del docente ----------

    /** Las asignaturas de un curso a las que el docente tiene acceso. */
    fun asignaturasPermitidas(docenteId: Long, cursoId: Long): Flow<List<AsignaturaEntity>> =
        docenteAsignaturaDao.obtenerAsignaturasPermitidas(docenteId, cursoId)

    suspend fun tieneAcceso(docenteId: Long, cursoId: Long, asignaturaId: Long): Boolean =
        docenteAsignaturaDao.tieneAcceso(docenteId, cursoId, asignaturaId)

    // ---------- Carga académica (Jefatura UTP) ----------

    fun cargaAcademica(cursoId: Long): Flow<List<DocenteAsignaturaEntity>> =
        docenteAsignaturaDao.obtenerPorCurso(cursoId)

    /**
     * Asigna un docente a una asignatura de un curso (reemplaza al anterior, si había).
     * @return false si el usuario no existe o no es docente.
     */
    suspend fun asignarDocente(cursoId: Long, asignaturaId: Long, docenteId: Long): Boolean {
        val docente = usuarioDao.obtenerPorId(docenteId) ?: return false
        if (docente.rol != Rol.DOCENTE) return false

        docenteAsignaturaDao.asignar(
            DocenteAsignaturaEntity(cursoId = cursoId, asignaturaId = asignaturaId, docenteId = docenteId)
        )
        return true
    }

    suspend fun quitarDocente(acceso: DocenteAsignaturaEntity) = docenteAsignaturaDao.quitar(acceso)
}