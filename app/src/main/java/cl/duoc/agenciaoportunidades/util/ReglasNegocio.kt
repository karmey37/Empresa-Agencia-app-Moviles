package cl.duoc.agenciaoportunidades.util

import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.ACTIVACION_CANCELADA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.ACTIVACION_REALIZADA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.CONTACTO_PENDIENTE
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.CONTACTO_REALIZADO
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.EN_ESPERA_RESPUESTA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.EN_EVALUACION
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.EVENTO_IDENTIFICADO
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.INFORMACION_INCOMPLETA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.OPORTUNIDAD_CERRADA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.OPORTUNIDAD_DESCARTADA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.PARTICIPACION_CONFIRMADA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.PROPUESTA_ENVIADA
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad.REQUIERE_SEGUIMIENTO
import cl.duoc.agenciaoportunidades.model.PerfilUsuario
import cl.duoc.agenciaoportunidades.model.Usuario

// Estados que solo puede asignar el supervisor:
// el ejecutivo no confirma ni descarta oportunidades de forma definitiva
private val ESTADOS_SOLO_SUPERVISOR = setOf(
    PARTICIPACION_CONFIRMADA,
    OPORTUNIDAD_DESCARTADA,
    ACTIVACION_CANCELADA,
    OPORTUNIDAD_CERRADA
)

// Cuando la oportunidad está en uno de estos estados, el ejecutivo ya no puede modificarla
private val ESTADOS_FINALES = setOf(
    OPORTUNIDAD_DESCARTADA,
    ACTIVACION_CANCELADA,
    OPORTUNIDAD_CERRADA
)

// Transiciones válidas: desde cada estado, a cuáles se puede pasar
private val TRANSICIONES: Map<EstadoOportunidad, List<EstadoOportunidad>> = mapOf(
    EVENTO_IDENTIFICADO to listOf(CONTACTO_PENDIENTE, EN_EVALUACION, INFORMACION_INCOMPLETA, OPORTUNIDAD_DESCARTADA),
    EN_EVALUACION to listOf(CONTACTO_PENDIENTE, INFORMACION_INCOMPLETA, OPORTUNIDAD_DESCARTADA),
    INFORMACION_INCOMPLETA to listOf(EN_EVALUACION, CONTACTO_PENDIENTE),
    CONTACTO_PENDIENTE to listOf(CONTACTO_REALIZADO, INFORMACION_INCOMPLETA, OPORTUNIDAD_DESCARTADA),
    CONTACTO_REALIZADO to listOf(PROPUESTA_ENVIADA, REQUIERE_SEGUIMIENTO, OPORTUNIDAD_DESCARTADA),
    PROPUESTA_ENVIADA to listOf(EN_ESPERA_RESPUESTA, REQUIERE_SEGUIMIENTO, PARTICIPACION_CONFIRMADA, OPORTUNIDAD_DESCARTADA),
    EN_ESPERA_RESPUESTA to listOf(REQUIERE_SEGUIMIENTO, PARTICIPACION_CONFIRMADA, OPORTUNIDAD_DESCARTADA),
    REQUIERE_SEGUIMIENTO to listOf(CONTACTO_REALIZADO, PROPUESTA_ENVIADA, EN_ESPERA_RESPUESTA, PARTICIPACION_CONFIRMADA, OPORTUNIDAD_DESCARTADA),
    PARTICIPACION_CONFIRMADA to listOf(ACTIVACION_REALIZADA, ACTIVACION_CANCELADA),
    ACTIVACION_REALIZADA to listOf(OPORTUNIDAD_CERRADA),
    ACTIVACION_CANCELADA to listOf(OPORTUNIDAD_CERRADA),
    OPORTUNIDAD_DESCARTADA to listOf(EN_EVALUACION),
    OPORTUNIDAD_CERRADA to emptyList()
)

fun esSupervisor(usuario: Usuario): Boolean {
    return usuario.perfil == PerfilUsuario.SUPERVISOR
}

// El supervisor ve todo; el ejecutivo solo lo que tiene asignado
fun puedeVerOportunidad(usuario: Usuario, responsableId: Int?): Boolean {
    return esSupervisor(usuario) || responsableId == usuario.id
}

fun puedeAsignarResponsable(usuario: Usuario): Boolean {
    return esSupervisor(usuario)
}

// Estados a los que este usuario puede pasar la oportunidad desde el estado actual
fun estadosPermitidos(usuario: Usuario, actual: EstadoOportunidad): List<EstadoOportunidad> {
    val supervisor = esSupervisor(usuario)
    if (!supervisor && actual in ESTADOS_FINALES) return emptyList()

    val siguientes = TRANSICIONES[actual].orEmpty()
    return if (supervisor) {
        siguientes
    } else {
        siguientes.filter { it !in ESTADOS_SOLO_SUPERVISOR }
    }
}

// Una oportunidad está "abierta" mientras no este descartada, cancelada, cerrada ni realizada
fun estaAbierta(estado: EstadoOportunidad): Boolean {
    return estado !in ESTADOS_FINALES && estado != ACTIVACION_REALIZADA
}

// El supervisor edita siempre, el ejecutivo solo mientras la oportunidad siga abierta
fun puedeEditarOportunidad(usuario: Usuario, estado: EstadoOportunidad): Boolean {
    return esSupervisor(usuario) || estaAbierta(estado)
}