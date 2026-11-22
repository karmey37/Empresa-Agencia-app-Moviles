package cl.duoc.agenciaoportunidades.data.externa

// Un feriado tal como lo entrega la API pública Nager.Date
// Ejemplo: {"date":"2026-12-25","localName":"Navidad","name":"Christmas Day","countryCode":"CL","global":true}
data class FeriadoDto(
    val date: String,          // formato ISO: yyyy-MM-dd
    val localName: String,     // nombre en español
    val name: String,          // nombre en inglés
    val countryCode: String? = null,
    val global: Boolean? = null
)
