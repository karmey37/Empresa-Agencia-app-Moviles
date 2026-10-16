package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import cl.duoc.agenciaoportunidades.model.Oportunidad
import cl.duoc.agenciaoportunidades.model.OportunidadResumen
import kotlinx.coroutines.flow.Flow

private const val SELECT_RESUMEN = """
    SELECT o.id AS id, o.nombre AS nombre, o.estado AS estado, o.prioridad AS prioridad,
           e.tipo AS tipo, e.fechaInicio AS fechaInicio,
           c.nombre AS comuna, c.region AS region,
           org.nombre AS organizacion,
           o.responsableId AS responsableId, u.nombre AS responsable,
           o.fechaActualizacion AS fechaActualizacion
    FROM oportunidades o
    INNER JOIN eventos e ON o.eventoId = e.id
    INNER JOIN comunas c ON e.comunaId = c.id
    INNER JOIN organizaciones org ON e.organizacionId = org.id
    LEFT JOIN usuarios u ON o.responsableId = u.id
"""

@Dao
interface OportunidadDao {

    @Insert
    suspend fun insertar(oportunidad: Oportunidad): Long

    @Update
    suspend fun actualizar(oportunidad: Oportunidad)

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Oportunidad?

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    fun observarPorId(id: Int): Flow<Oportunidad?>

    // Supervisor: todas las oportunidades
    @Query(SELECT_RESUMEN + " ORDER BY e.fechaInicio ASC")
    fun obtenerResumenes(): Flow<List<OportunidadResumen>>

    // Ejecutivo: solo las que tiene asignadas
    @Query(SELECT_RESUMEN + " WHERE o.responsableId = :usuarioId ORDER BY e.fechaInicio ASC")
    fun obtenerResumenesPorResponsable(usuarioId: Int): Flow<List<OportunidadResumen>>

    // Oportunidades que nunca han tenido un seguimiento
    @Query("SELECT * FROM oportunidades WHERE id NOT IN (SELECT oportunidadId FROM seguimientos)")
    fun obtenerSinSeguimiento(): Flow<List<Oportunidad>>
}