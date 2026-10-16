package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.agenciaoportunidades.model.ResultadoActivacion
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultadoActivacionDao {

    @Insert
    suspend fun insertar(resultado: ResultadoActivacion): Long

    @Query("SELECT * FROM resultados_activacion WHERE oportunidadId = :oportunidadId")
    fun obtenerPorOportunidad(oportunidadId: Int): Flow<List<ResultadoActivacion>>
}