package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import cl.duoc.agenciaoportunidades.model.Organizacion
import cl.duoc.agenciaoportunidades.model.OrganizacionResumen
import kotlinx.coroutines.flow.Flow

private const val SELECT_RESUMEN_ORGANIZACION = """
    SELECT org.id AS id, org.nombre AS nombre, org.tipo AS tipo, org.rubro AS rubro,
           c.nombre AS comuna, c.region AS region,
           (SELECT COUNT(*) FROM contactos WHERE organizacionId = org.id) AS cantidadContactos,
           (SELECT COUNT(*) FROM eventos WHERE organizacionId = org.id) AS cantidadEventos
    FROM organizaciones org
    INNER JOIN comunas c ON org.comunaId = c.id
    ORDER BY org.nombre
"""

@Dao
interface OrganizacionDao {

    @Insert
    suspend fun insertar(organizacion: Organizacion): Long

    @Update
    suspend fun actualizar(organizacion: Organizacion)

    @Query("SELECT * FROM organizaciones ORDER BY nombre")
    fun obtenerTodas(): Flow<List<Organizacion>>

    @Query("SELECT * FROM organizaciones WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Organizacion?

    @Query("SELECT * FROM organizaciones WHERE id = :id")
    fun observarPorId(id: Int): Flow<Organizacion?>

    @Query(SELECT_RESUMEN_ORGANIZACION)
    fun obtenerResumenes(): Flow<List<OrganizacionResumen>>
}