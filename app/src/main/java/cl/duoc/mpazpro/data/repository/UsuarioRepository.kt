package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.dao.CursoDao
import cl.duoc.mpazpro.data.local.dao.DocenteAsignaturaDao
import cl.duoc.mpazpro.data.local.dao.UsuarioDao
import cl.duoc.mpazpro.data.local.entity.AsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.CursoEntity
import cl.duoc.mpazpro.data.local.entity.DocenteAsignaturaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.data.local.relation.CursoConEstudiantes
import cl.duoc.mpazpro.data.seguridad.PasswordHasher
import cl.duoc.mpazpro.domain.model.Rol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Resultado de intentar iniciar sesión. */
sealed class ResultadoLogin {
    data class Exito(val usuario: UsuarioEntity) : ResultadoLogin()
    data object CredencialesInvalidas : ResultadoLogin()
    data object CuentaInactiva : ResultadoLogin()
}

/** Resultado de que Jefatura UTP cree o modifique una cuenta. */
enum class ResultadoCuenta {
    OK,
    SIN_PERMISO,            // quien lo intenta no es Jefatura UTP
    NOMBRE_USUARIO_EN_USO,
    DATOS_INVALIDOS,        // nombre vacío, contraseña muy corta, etc.
    CURSO_REQUERIDO,        // un estudiante debe pertenecer a un curso
    NO_ENCONTRADO
}

/** Usuarios, login, cursos, carga académica y permisos de los docentes. */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val cursoDao: CursoDao,
    private val docenteAsignaturaDao: DocenteAsignaturaDao
) {

    companion object {
        const val LARGO_MINIMO_PASSWORD = 4
    }

    // ---------- Inicio de sesión ----------

    /**
     * Verifica usuario y contraseña.
     * Si el usuario no existe o la contraseña es incorrecta, el resultado es el MISMO,
     * para no revelar qué nombres de usuario existen.
     */
    suspend fun iniciarSesion(nombreUsuario: String, password: String): ResultadoLogin {
        val usuario = usuarioDao.buscarPorNombreUsuario(normalizar(nombreUsuario))
            ?: return ResultadoLogin.CredencialesInvalidas

        val coincide = withContext(Dispatchers.Default) {
            PasswordHasher.verificar(password, usuario.passwordHash, usuario.passwordSal)
        }
        if (!coincide) return ResultadoLogin.CredencialesInvalidas
        if (!usuario.activo) return ResultadoLogin.CuentaInactiva

        return ResultadoLogin.Exito(usuario)
    }

    // ---------- Administración de cuentas (solo Jefatura UTP) ----------

    fun todosLosUsuarios(): Flow<List<UsuarioEntity>> = usuarioDao.obtenerTodos()

    /** Jefatura UTP crea una cuenta de estudiante o de docente. */
    suspend fun crearUsuario(
        creadorId: Long,
        nombre: String,
        nombreUsuario: String,
        password: String,
        rol: Rol,
        cursoId: Long? = null
    ): ResultadoCuenta {
        if (!esJefaturaUtp(creadorId)) return ResultadoCuenta.SIN_PERMISO
        if (rol == Rol.JEFATURA_UTP) return ResultadoCuenta.SIN_PERMISO

        val usuarioNormalizado = normalizar(nombreUsuario)
        if (nombre.isBlank() || usuarioNormalizado.isBlank()) return ResultadoCuenta.DATOS_INVALIDOS
        if (password.length < LARGO_MINIMO_PASSWORD) return ResultadoCuenta.DATOS_INVALIDOS
        if (rol == Rol.ESTUDIANTE && cursoId == null) return ResultadoCuenta.CURSO_REQUERIDO
        if (usuarioDao.existeNombreUsuario(usuarioNormalizado)) return ResultadoCuenta.NOMBRE_USUARIO_EN_USO

        val credencial = withContext(Dispatchers.Default) { PasswordHasher.generar(password) }

        usuarioDao.insertar(
            UsuarioEntity(
                nombre = nombre.trim(),
                nombreUsuario = usuarioNormalizado,
                passwordHash = credencial.hash,
                passwordSal = credencial.sal,
                rol = rol,
                cursoId = if (rol == Rol.ESTUDIANTE) cursoId else null
            )
        )
        return ResultadoCuenta.OK
    }

    /** Jefatura UTP asigna una contraseña nueva (por ejemplo, si alguien la olvidó). */
    suspend fun restablecerPassword(creadorId: Long, usuarioId: Long, nuevaPassword: String): ResultadoCuenta {
        if (!esJefaturaUtp(creadorId)) return ResultadoCuenta.SIN_PERMISO
        if (nuevaPassword.length < LARGO_MINIMO_PASSWORD) return ResultadoCuenta.DATOS_INVALIDOS
        val usuario = usuarioDao.obtenerPorId(usuarioId) ?: return ResultadoCuenta.NO_ENCONTRADO

        val credencial = withContext(Dispatchers.Default) { PasswordHasher.generar(nuevaPassword) }
        usuarioDao.actualizar(usuario.copy(passwordHash = credencial.hash, passwordSal = credencial.sal))
        return ResultadoCuenta.OK
    }

    /** Jefatura UTP activa o desactiva una cuenta (sin borrar sus datos). */
    suspend fun cambiarActivo(creadorId: Long, usuarioId: Long, activo: Boolean): ResultadoCuenta {
        if (!esJefaturaUtp(creadorId)) return ResultadoCuenta.SIN_PERMISO
        val usuario = usuarioDao.obtenerPorId(usuarioId) ?: return ResultadoCuenta.NO_ENCONTRADO
        if (usuario.rol == Rol.JEFATURA_UTP) return ResultadoCuenta.SIN_PERMISO

        usuarioDao.actualizar(usuario.copy(activo = activo))
        return ResultadoCuenta.OK
    }

    // ---------- Perfiles ----------

    fun perfilesPorRol(rol: Rol): Flow<List<UsuarioEntity>> = usuarioDao.obtenerPorRol(rol)

    suspend fun usuario(id: Long): UsuarioEntity? = usuarioDao.obtenerPorId(id)

    // ---------- Cursos ----------

    fun todosLosCursos(): Flow<List<CursoEntity>> = cursoDao.obtenerTodos()

    fun cursosDelDocente(docenteId: Long): Flow<List<CursoEntity>> =
        cursoDao.obtenerVisiblesParaDocente(docenteId)

    fun cursoConEstudiantes(cursoId: Long): Flow<CursoConEstudiantes?> = cursoDao.obtenerConEstudiantes(cursoId)

    suspend fun curso(cursoId: Long): CursoEntity? = cursoDao.obtenerPorId(cursoId)

    // ---------- Permisos del docente ----------

    fun asignaturasPermitidas(docenteId: Long, cursoId: Long): Flow<List<AsignaturaEntity>> =
        docenteAsignaturaDao.obtenerAsignaturasPermitidas(docenteId, cursoId)

    suspend fun tieneAcceso(docenteId: Long, cursoId: Long, asignaturaId: Long): Boolean =
        docenteAsignaturaDao.tieneAcceso(docenteId, cursoId, asignaturaId)

    // ---------- Carga académica (Jefatura UTP) ----------

    fun cargaAcademica(cursoId: Long): Flow<List<DocenteAsignaturaEntity>> =
        docenteAsignaturaDao.obtenerPorCurso(cursoId)

    suspend fun asignarDocente(cursoId: Long, asignaturaId: Long, docenteId: Long): Boolean {
        val docente = usuarioDao.obtenerPorId(docenteId) ?: return false
        if (docente.rol != Rol.DOCENTE) return false

        docenteAsignaturaDao.asignar(
            DocenteAsignaturaEntity(cursoId = cursoId, asignaturaId = asignaturaId, docenteId = docenteId)
        )
        return true
    }

    suspend fun quitarDocente(acceso: DocenteAsignaturaEntity) = docenteAsignaturaDao.quitar(acceso)

    // ---------- Ayudantes internos ----------

    private suspend fun esJefaturaUtp(usuarioId: Long): Boolean =
        usuarioDao.obtenerPorId(usuarioId)?.rol == Rol.JEFATURA_UTP

    /** "  Tomas " → "tomas": así no importa cómo lo escriban. */
    private fun normalizar(nombreUsuario: String): String = nombreUsuario.trim().lowercase()
}