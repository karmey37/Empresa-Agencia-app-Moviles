package cl.duoc.agenciaoportunidades.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "organizaciones")
data class Organizacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,
    val rubro: String = "",
    val comuna: String = "",
    val region: String = "",
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = ""
)