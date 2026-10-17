package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.data.local.relation.RendimientoOa
import kotlinx.coroutines.flow.Flow

@Dao
interface RespuestaDao {

    /**
     * Guarda la respuesta apenas el niño la da ("guardado en tiempo real").
     * REPLACE: si ya había respondido esa pregunta en este intento, se reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(respuesta: RespuestaEntity)

    /** Las respuestas de un intento (para mostrar qué respondió bien y qué mal). */
    @Query("SELECT * FROM respuesta WHERE intentoId = :intentoId")
    fun obtenerPorIntento(intentoId: Long): Flow<List<RespuestaEntity>>

    /** Cuántas correctas lleva en el intento (para calcular el resultado al finalizar). */
    @Query("SELECT COUNT(*) FROM respuesta WHERE intentoId = :intentoId AND esCorrecta = 1")
    suspend fun contarCorrectas(intentoId: Long): Int

    /**
     * EL INFORME: rendimiento de un estudiante por OA en una unidad.
     * Usa el MEJOR intento terminado de la prueba final.
     */
    @Query(
        """
        SELECT oa.id          AS oaId,
               oa.codigo      AS codigo,
               oa.descripcion AS descripcion,
               SUM(CASE WHEN r.esCorrecta = 1 THEN 1 ELSE 0 END) AS correctas,
               COUNT(r.id)    AS total
        FROM respuesta r
        JOIN pregunta p              ON p.id = r.preguntaId
        JOIN objetivo_aprendizaje oa ON oa.id = p.oaId
        WHERE r.intentoId = (
            SELECT i.id FROM intento i
            JOIN actividad a ON a.id = i.actividadId
            WHERE i.estudianteId = :estudianteId
              AND a.unidadId = :unidadId
              AND a.tipo = 'PRUEBA_FINAL'
              AND i.fechaFin IS NOT NULL
            ORDER BY i.correctas DESC, i.numeroIntento DESC
            LIMIT 1
        )
        GROUP BY oa.id
        ORDER BY oa.codigo
        """
    )
    fun obtenerRendimientoPorOa(estudianteId: Long, unidadId: Long): Flow<List<RendimientoOa>>
}