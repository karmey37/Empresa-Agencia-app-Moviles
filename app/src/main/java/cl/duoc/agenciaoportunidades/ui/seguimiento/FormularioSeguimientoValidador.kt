package cl.duoc.agenciaoportunidades.ui.seguimiento

import cl.duoc.agenciaoportunidades.util.diasHasta
import cl.duoc.agenciaoportunidades.util.parsearFecha
import cl.duoc.agenciaoportunidades.util.validarFecha
import cl.duoc.agenciaoportunidades.util.validarTextoObligatorio

// Devuelve un mapa campo -> mensaje de error. Si está vacío, el formulario es válido.
fun validarFormularioSeguimiento(
    estado: FormularioSeguimientoUiState,
    ahora: Long
): Map<CampoSeguimiento, String> {
    val errores = mutableMapOf<CampoSeguimiento, String>()

    // ----- Fecha de la gestión: obligatoria y no puede ser futura -----
    val errorFecha = validarFecha(estado.fecha, "La fecha de la gestión")
    val fechaGestion = parsearFecha(estado.fecha)
    if (errorFecha != null) {
        errores[CampoSeguimiento.FECHA] = errorFecha
    } else if (fechaGestion != null && diasHasta(fechaGestion, ahora) > 0) {
        errores[CampoSeguimiento.FECHA] = "La fecha de la gestión no puede ser futura"
    }

    validarTextoObligatorio(estado.resultado, "El resultado", 5, 200)
        ?.let { errores[CampoSeguimiento.RESULTADO] = it }

    // ----- Próxima acción y su fecha: se piden juntas -----
    val hayAccion = estado.proximaAccion.isNotBlank()
    val hayFecha = estado.fechaProxima.isNotBlank()

    if (hayAccion) {
        validarTextoObligatorio(estado.proximaAccion, "La próxima acción", 3, 100)
            ?.let { errores[CampoSeguimiento.PROXIMA_ACCION] = it }
    } else if (hayFecha) {
        errores[CampoSeguimiento.PROXIMA_ACCION] = "Describe la próxima acción"
    }

    if (hayFecha) {
        val errorProxima = validarFecha(estado.fechaProxima, "La fecha del próximo seguimiento")
        val fechaProxima = parsearFecha(estado.fechaProxima)
        if (errorProxima != null) {
            errores[CampoSeguimiento.FECHA_PROXIMA] = errorProxima
        } else if (fechaGestion != null && fechaProxima != null && fechaProxima < fechaGestion) {
            errores[CampoSeguimiento.FECHA_PROXIMA] =
                "El próximo seguimiento no puede ser anterior a la fecha de la gestión"
        }
    } else if (hayAccion) {
        errores[CampoSeguimiento.FECHA_PROXIMA] = "Indica la fecha del próximo seguimiento"
    }

    return errores
}