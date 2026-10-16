package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "comunas",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Comuna(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val region: String
)