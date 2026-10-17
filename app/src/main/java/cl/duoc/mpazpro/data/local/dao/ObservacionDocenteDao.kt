package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.mpazpro.data.local.entity.ObservacionDocenteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservacionDocenteDao {

    /** La observación del informe de un estudiante en una unidad (puede no existir aún). */
    @Query("SELECT * FROM observacion_docente WHERE estudianteId = :estudianteId AND unidadId = :unidadId")
    fun obtener(estudianteId: Long, unidadId: Long): Flow<ObservacionDocenteEntity?>

    /** Crea o reemplaza la observación (una por estudiante y unidad). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(observacion: ObservacionDocenteEntity)

    /** Marca el informe como validado por el docente. */
    @Query("UPDATE observacion_docente SET validado = 1 WHERE estudianteId = :estudianteId AND unidadId = :unidadId")
    suspend fun validar(estudianteId: Long, unidadId: Long)
}