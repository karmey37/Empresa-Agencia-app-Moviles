package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "contactos",
    foreignKeys = [
        ForeignKey(
            entity = Organizacion::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("organizacionId")]
)
data class Contacto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val organizacionId: Int,
    val nombre: String,
    val cargo: String = "",
    val area: AreaContacto = AreaContacto.EVENTOS,
    val correo: String = "",
    val telefono: String = "",
    val medioPreferente: MedioContacto = MedioContacto.CORREO,
    val estado: EstadoContacto = EstadoContacto.ACTIVO,
    val fechaUltimoContacto: Long? = null,
    val observaciones: String = ""
)