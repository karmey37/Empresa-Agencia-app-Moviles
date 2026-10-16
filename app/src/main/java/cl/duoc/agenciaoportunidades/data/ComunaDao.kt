package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import cl.duoc.agenciaoportunidades.model.Comuna
import kotlinx.coroutines.flow.Flow

@Dao
interface ComunaDao {

    @Insert
    suspend fun insertar(comuna: Comuna): Long

    @Query("SELECT * FROM comunas ORDER BY nombre")
    fun obtenerTodas(): Flow<List<Comuna>>
}