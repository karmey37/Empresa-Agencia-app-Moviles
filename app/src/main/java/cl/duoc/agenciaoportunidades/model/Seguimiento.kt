package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "seguimientos",
    foreignKeys = [
        ForeignKey(
            entity = Oportunidad::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Contacto::class,
            parentColumns = ["id"],
            childColumns = ["contactoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("oportunidadId"), Index("usuarioId"), Index("contactoId")]
)
data class Seguimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oportunidadId: Int,
    val usuarioId: Int? = null,
    val contactoId: Int? = null,
    val tipoAccion: TipoAccion = TipoAccion.INVESTIGACION_INICIAL,
    val fechaHora: Long = System.currentTimeMillis(),
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProximoSeguimiento: Long? = null,
    val observaciones: String = ""
)