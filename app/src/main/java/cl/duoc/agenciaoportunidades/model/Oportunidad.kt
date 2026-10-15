package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "oportunidades",
    foreignKeys = [
        ForeignKey(
            entity = Evento::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("eventoId"), Index("responsableId")]
)
data class Oportunidad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventoId: Int,
    val responsableId: Int? = null,
    val nombre: String,
    val tipo: TipoEvento = TipoEvento.EVENTO_CORPORATIVO,
    val descripcion: String = "",
    val fuente: String = "",
    val fechaDeteccion: Long = System.currentTimeMillis(),
    val prioridad: Prioridad = Prioridad.MEDIA,
    val potencialComercial: Prioridad = Prioridad.MEDIA,
    val afinidadPublico: Prioridad = Prioridad.MEDIA,
    val estado: EstadoOportunidad = EstadoOportunidad.EVENTO_IDENTIFICADO,
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val observaciones: String = ""
)