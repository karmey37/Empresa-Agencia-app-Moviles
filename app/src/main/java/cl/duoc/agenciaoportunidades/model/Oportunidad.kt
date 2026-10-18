package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(
    tableName = "oportunidades",
    foreignKeys = [ForeignKey(
        entity = Organizacion::class,
        parentColumns = ["id"],
        childColumns = ["organizacionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Oportunidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val organizacionId: Int,
    val estado: String = "Evento identificado",
    val prioridad: String = "Media",
    val uriFotografia: String? = null
)