package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "resultados_activacion",
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
data class ResultadoActivacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oportunidadId: Int,
    val fechaRealizacion: Long,
    val estadoFinal: EstadoOportunidad = EstadoOportunidad.ACTIVACION_REALIZADA,
    val asistentesEstimados: Int = 0,
    val contactosGenerados: Int = 0,
    val personasInteresadas: Int = 0,
    val inscripciones: Int = 0,
    val evaluacionGeneral: Int = 0,
    val aprendizajes: String = "",
    val observaciones: String = ""
)