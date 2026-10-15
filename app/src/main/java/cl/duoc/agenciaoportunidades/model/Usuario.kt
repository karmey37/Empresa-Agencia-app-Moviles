package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val perfil: PerfilUsuario = PerfilUsuario.EJECUTIVO,
    val area: String = "Comercial",
    val zona: String = "",
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)