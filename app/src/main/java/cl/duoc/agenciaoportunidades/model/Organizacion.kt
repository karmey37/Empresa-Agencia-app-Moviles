package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "organizaciones",
    foreignKeys = [
        ForeignKey(
            entity = Comuna::class,
            parentColumns = ["id"],
            childColumns = ["comunaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("comunaId")]
)
data class Organizacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,
    val rubro: String = "",
    val comunaId: Int,
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = ""
)