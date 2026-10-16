package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import cl.duoc.agenciaoportunidades.model.Organizacion
import kotlinx.coroutines.flow.Flow

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
}