package cl.duoc.agenciaoportunidades.ui.organizaciones

import cl.duoc.agenciaoportunidades.util.validarSitioWebOpcional
import cl.duoc.agenciaoportunidades.util.validarTextoObligatorio

fun validarFormularioOrganizacion(
    estado: FormularioOrganizacionUiState
): Map<CampoOrganizacion, String> {
    val errores = mutableMapOf<CampoOrganizacion, String>()

    validarTextoObligatorio(estado.nombre, "El nombre", 3, 100)
        ?.let { errores[CampoOrganizacion.NOMBRE] = it }
    validarTextoObligatorio(estado.rubro, "El rubro", 3, 60)
        ?.let { errores[CampoOrganizacion.RUBRO] = it }

    if (estado.comunaId == null) {
        errores[CampoOrganizacion.COMUNA] = "Selecciona una comuna"
    }

    validarSitioWebOpcional(estado.sitioWeb)
        ?.let { errores[CampoOrganizacion.SITIO_WEB] = it }

    return errores
}