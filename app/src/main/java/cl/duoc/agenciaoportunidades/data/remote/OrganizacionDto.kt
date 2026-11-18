package cl.duoc.agenciaoportunidades.data.remote

data class OrganizacionDto(
    val id: Long? = null,
    val nombre: String,
    val tipo: String,
    val rubro: String,
    val comuna: String,
    val sitioWeb: String? = null,
    val publicoAsociado: String? = null,
    val observaciones: String? = null
)