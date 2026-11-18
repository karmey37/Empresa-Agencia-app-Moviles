package cl.duoc.agenciaoportunidades.ui.directorio

import cl.duoc.agenciaoportunidades.util.validarSitioWebOpcional
import cl.duoc.agenciaoportunidades.util.validarTextoObligatorio

fun validarDirectorio(estado: DirectorioUiState): Map<CampoDirectorio, String> {
    val errores = mutableMapOf<CampoDirectorio, String>()

    validarTextoObligatorio(estado.nombre, "El nombre", 3, 100)
        ?.let { errores[CampoDirectorio.NOMBRE] = it }
    validarTextoObligatorio(estado.rubro, "El rubro", 3, 60)
        ?.let { errores[CampoDirectorio.RUBRO] = it }
    validarTextoObligatorio(estado.comuna, "La comuna", 3, 60)
        ?.let { errores[CampoDirectorio.COMUNA] = it }
    validarSitioWebOpcional(estado.sitioWeb)
        ?.let { errores[CampoDirectorio.SITIO_WEB] = it }

    return errores
}