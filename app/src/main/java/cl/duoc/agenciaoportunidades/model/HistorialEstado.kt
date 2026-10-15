package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "historial_estados",
    foreignKeys = [
        ForeignKey(
            entity = Oportunidad::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("oportunidadId")]
)
data class HistorialEstado(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oportunidadId: Int,
    val estadoAnterior: EstadoOportunidad? = null,
    val estadoNuevo: EstadoOportunidad,
    val fecha: Long = System.currentTimeMillis(),
    val usuarioId: Int? = null,
    val observaciones: String = ""
)