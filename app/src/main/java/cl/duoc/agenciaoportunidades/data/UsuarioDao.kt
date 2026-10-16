package cl.duoc.agenciaoportunidades.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import cl.duoc.agenciaoportunidades.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)

    // Devuelve el usuario si correo y contraseña coinciden y la cuenta está activa
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password AND activo = 1 LIMIT 1")
    suspend fun iniciarSesion(correo: String, password: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE perfil = 'EJECUTIVO' AND activo = 1 ORDER BY nombre")
    fun obtenerEjecutivos(): Flow<List<Usuario>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}