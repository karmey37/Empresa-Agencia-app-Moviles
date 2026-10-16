package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.agenciaoportunidades.model.HistorialEstado
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialEstadoDao {

    @Insert
    suspend fun insertar(historial: HistorialEstado): Long

    @Query("SELECT * FROM historial_estados WHERE oportunidadId = :oportunidadId ORDER BY fecha DESC")
    fun obtenerPorOportunidad(oportunidadId: Int): Flow<List<HistorialEstado>>
}