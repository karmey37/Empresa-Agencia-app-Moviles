package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.agenciaoportunidades.model.Evidencia
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Insert
    suspend fun insertar(evidencia: Evidencia): Long

    @Delete
    suspend fun eliminar(evidencia: Evidencia)

    @Query("SELECT * FROM evidencias WHERE oportunidadId = :oportunidadId ORDER BY fecha DESC")
    fun obtenerPorOportunidad(oportunidadId: Int): Flow<List<Evidencia>>
}