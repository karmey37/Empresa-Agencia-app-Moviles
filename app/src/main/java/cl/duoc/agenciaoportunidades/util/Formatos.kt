package cl.duoc.agenciaoportunidades.util

import cl.duoc.agenciaoportunidades.model.AreaContacto
import cl.duoc.agenciaoportunidades.model.EstadoContacto
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.MedioContacto
import cl.duoc.agenciaoportunidades.model.PerfilUsuario
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.TipoAccion
import cl.duoc.agenciaoportunidades.model.TipoEvento
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatearFecha(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(millis))
}

fun formatearFechaHora(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(millis))
}

fun formatearPesos(valor: Int): String {
    val formato = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL"))
    return "\$" + formato.format(valor)
}

fun EstadoOportunidad.etiqueta(): String = when (this) {
    EstadoOportunidad.EVENTO_IDENTIFICADO -> "Evento identificado"
    EstadoOportunidad.CONTACTO_PENDIENTE -> "Contacto pendiente"
    EstadoOportunidad.CONTACTO_REALIZADO -> "Contacto realizado"
    EstadoOportunidad.PROPUESTA_ENVIADA -> "Propuesta enviada"
    EstadoOportunidad.EN_ESPERA_RESPUESTA -> "En espera de respuesta"
    EstadoOportunidad.PARTICIPACION_CONFIRMADA -> "Participación confirmada"
    EstadoOportunidad.OPORTUNIDAD_DESCARTADA -> "Oportunidad descartada"
    EstadoOportunidad.EN_EVALUACION -> "En evaluación"
    EstadoOportunidad.INFORMACION_INCOMPLETA -> "Información incompleta"
    EstadoOportunidad.REQUIERE_SEGUIMIENTO -> "Requiere seguimiento"
    EstadoOportunidad.ACTIVACION_REALIZADA -> "Activación realizada"
    EstadoOportunidad.ACTIVACION_CANCELADA -> "Activación cancelada"
    EstadoOportunidad.OPORTUNIDAD_CERRADA -> "Oportunidad cerrada"
}

fun Prioridad.etiqueta(): String = when (this) {
    Prioridad.BAJA -> "Baja"
    Prioridad.MEDIA -> "Media"
    Prioridad.ALTA -> "Alta"
}

fun TipoEvento.etiqueta(): String = when (this) {
    TipoEvento.EVENTO_CORPORATIVO -> "Evento corporativo"
    TipoEvento.FERIA_EMPRESARIAL -> "Feria empresarial"
    TipoEvento.ACTIVIDAD_MUNICIPAL -> "Actividad municipal"
    TipoEvento.JORNADA_BENEFICIOS -> "Jornada de beneficios"
    TipoEvento.FERIA_INSTITUCIONAL -> "Feria institucional"
    TipoEvento.ACTIVIDAD_TRABAJADORES -> "Actividad de trabajadores"
    TipoEvento.ACTIVACION_COMERCIAL -> "Activación comercial"
    TipoEvento.OTRA -> "Otra"
}

fun TipoAccion.etiqueta(): String = when (this) {
    TipoAccion.INVESTIGACION_INICIAL -> "Investigación inicial"
    TipoAccion.BUSQUEDA_CONTACTO -> "Búsqueda de contacto"
    TipoAccion.PRIMER_CONTACTO -> "Primer contacto"
    TipoAccion.ENVIO_PRESENTACION -> "Envío de presentación"
    TipoAccion.ENVIO_PROPUESTA -> "Envío de propuesta"
    TipoAccion.SOLICITUD_ANTECEDENTES -> "Solicitud de antecedentes"
    TipoAccion.REUNION -> "Reunión"
    TipoAccion.LLAMADA -> "Llamada"
    TipoAccion.SEGUIMIENTO -> "Seguimiento"
    TipoAccion.RECEPCION_RESPUESTA -> "Recepción de respuesta"
    TipoAccion.CONFIRMACION -> "Confirmación"
    TipoAccion.CIERRE -> "Cierre"
}

fun PerfilUsuario.etiqueta(): String = when (this) {
    PerfilUsuario.EJECUTIVO -> "Ejecutivo Comercial"
    PerfilUsuario.SUPERVISOR -> "Supervisor Comercial"
}

fun TipoOrganizacion.etiqueta(): String = when (this) {
    TipoOrganizacion.EMPRESA -> "Empresa"
    TipoOrganizacion.MUNICIPALIDAD -> "Municipalidad"
    TipoOrganizacion.INSTITUCION_PUBLICA -> "Institución pública"
    TipoOrganizacion.INSTITUCION_PRIVADA -> "Institución privada"
    TipoOrganizacion.ORGANIZADOR_EVENTOS -> "Organizador de eventos"
    TipoOrganizacion.CENTRO_COMERCIAL -> "Centro comercial"
    TipoOrganizacion.ASOCIACION -> "Asociación"
    TipoOrganizacion.OTRA -> "Otra entidad"
}

fun AreaContacto.etiqueta(): String = when (this) {
    AreaContacto.EVENTOS -> "Eventos"
    AreaContacto.ACTIVACIONES -> "Activaciones"
    AreaContacto.MARKETING -> "Marketing"
    AreaContacto.COMUNICACIONES -> "Comunicaciones"
    AreaContacto.RECURSOS_HUMANOS -> "Recursos Humanos"
    AreaContacto.BIENESTAR -> "Bienestar"
    AreaContacto.BENEFICIOS -> "Beneficios"
    AreaContacto.ADMINISTRACION -> "Administración"
}

fun MedioContacto.etiqueta(): String = when (this) {
    MedioContacto.CORREO -> "Correo"
    MedioContacto.TELEFONO -> "Teléfono"
    MedioContacto.REUNION -> "Reunión"
    MedioContacto.OTRO -> "Otro"
}

fun EstadoContacto.etiqueta(): String = when (this) {
    EstadoContacto.ACTIVO -> "Activo"
    EstadoContacto.SIN_RESPUESTA -> "Sin respuesta"
    EstadoContacto.INACTIVO -> "Inactivo"
}