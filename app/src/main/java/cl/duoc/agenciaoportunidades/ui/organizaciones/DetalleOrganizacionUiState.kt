package cl.duoc.agenciaoportunidades.ui.organizaciones

import cl.duoc.agenciaoportunidades.model.Comuna
import cl.duoc.agenciaoportunidades.model.Contacto
import cl.duoc.agenciaoportunidades.model.Evento
import cl.duoc.agenciaoportunidades.model.Organizacion

data class DetalleOrganizacionUiState(
    val cargando: Boolean = true,
    val organizacion: Organizacion? = null,
    val comuna: Comuna? = null,
    val contactos: List<Contacto> = emptyList(),
    val eventos: List<Evento> = emptyList()
)