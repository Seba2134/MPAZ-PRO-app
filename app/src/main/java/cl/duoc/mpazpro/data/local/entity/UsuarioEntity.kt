package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.mpazpro.domain.model.Rol

@Entity(
    tableName = "usuario",
    foreignKeys = [
        ForeignKey(
            entity = CursoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("cursoId"),
        Index(value = ["nombreUsuario"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,              // nombre visible
    val nombreUsuario: String,       // para iniciar sesión: "tomas" (unico, en minúsculas)
    val passwordHash: String,        // NUNCA la contraseña: solo su hash
    val passwordSal: String,
    val rol: Rol,
    val cursoId: Long? = null,       // solo los estudiantes tienen curso
    val activo: Boolean = true       // UTP puede desactivar una cuenta sin borrarla
)