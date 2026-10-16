package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "eventos",
    foreignKeys = [
        ForeignKey(
            entity = Organizacion::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Comuna::class,
            parentColumns = ["id"],
            childColumns = ["comunaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("organizacionId"), Index("comunaId")]
)
data class Evento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val organizacionId: Int,
    val comunaId: Int,
    val nombre: String,
    val tipo: TipoEvento = TipoEvento.EVENTO_CORPORATIVO,
    val descripcion: String = "",
    val fechaInicio: Long,
    val fechaTermino: Long,
    val horario: String = "",
    val ubicacion: String = "",
    val publicoEsperado: String = "",
    val publicoObjetivo: String = "",
    val asistentesEstimados: Int = 0,
    val condiciones: String = "",
    val requisitos: String = "",
    val costoReferencial: Int = 0,
    val espaciosDisponibles: Int = 0,
    val observaciones: String = ""
)