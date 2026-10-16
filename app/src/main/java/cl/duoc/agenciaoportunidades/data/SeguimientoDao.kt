package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.agenciaoportunidades.model.Seguimiento
import cl.duoc.agenciaoportunidades.model.SeguimientoDetalle
import kotlinx.coroutines.flow.Flow

private const val SELECT_DETALLE = """
    SELECT s.id AS id, s.oportunidadId AS oportunidadId, o.nombre AS oportunidadNombre,
           o.responsableId AS responsableId, s.tipoAccion AS tipoAccion,
           s.fechaHora AS fechaHora, s.resultado AS resultado,
           s.proximaAccion AS proximaAccion,
           s.fechaProximoSeguimiento AS fechaProximoSeguimiento,
           s.observaciones AS observaciones,
           u.nombre AS usuario, c.nombre AS contacto
    FROM seguimientos s
    INNER JOIN oportunidades o ON s.oportunidadId = o.id
    LEFT JOIN usuarios u ON s.usuarioId = u.id
    LEFT JOIN contactos c ON s.contactoId = c.id
"""

// Solo cuenta el último seguimiento de cada oportunidad y excluye las oportunidades ya cerradas
private const val SOLO_ULTIMO_ABIERTO = """
    s.fechaProximoSeguimiento IS NOT NULL
    AND s.id = (SELECT MAX(s2.id) FROM seguimientos s2 WHERE s2.oportunidadId = s.oportunidadId)
    AND o.estado NOT IN ('OPORTUNIDAD_DESCARTADA', 'OPORTUNIDAD_CERRADA',
                         'ACTIVACION_REALIZADA', 'ACTIVACION_CANCELADA')
"""

@Dao
interface SeguimientoDao {

    @Insert
    suspend fun insertar(seguimiento: Seguimiento): Long

    // Historial completo de una oportunidad, el más reciente primero
    @Query(SELECT_DETALLE + " WHERE s.oportunidadId = :oportunidadId ORDER BY s.fechaHora DESC")
    fun obtenerPorOportunidad(oportunidadId: Int): Flow<List<SeguimientoDetalle>>

    // Seguimientos cuya fecha ya pasó: gestiones atrasadas
    @Query(SELECT_DETALLE + " WHERE " + SOLO_ULTIMO_ABIERTO +
            " AND s.fechaProximoSeguimiento < :ahora ORDER BY s.fechaProximoSeguimiento ASC")
    fun obtenerAtrasados(ahora: Long): Flow<List<SeguimientoDetalle>>

    // Seguimientos programados a futuro: gestiones pendientes
    @Query(SELECT_DETALLE + " WHERE " + SOLO_ULTIMO_ABIERTO +
            " AND s.fechaProximoSeguimiento >= :ahora ORDER BY s.fechaProximoSeguimiento ASC")
    fun obtenerPendientes(ahora: Long): Flow<List<SeguimientoDetalle>>
}