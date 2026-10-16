package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import cl.duoc.agenciaoportunidades.model.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    @Insert
    suspend fun insertar(evento: Evento): Long

    @Update
    suspend fun actualizar(evento: Evento)

    @Query("SELECT * FROM eventos ORDER BY fechaInicio")
    fun obtenerTodos(): Flow<List<Evento>>

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Evento?
}