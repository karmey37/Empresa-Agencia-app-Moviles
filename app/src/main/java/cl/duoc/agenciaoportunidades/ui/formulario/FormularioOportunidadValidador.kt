package cl.duoc.agenciaoportunidades.ui.formulario

import cl.duoc.agenciaoportunidades.util.diasHasta
import cl.duoc.agenciaoportunidades.util.parsearFecha
import cl.duoc.agenciaoportunidades.util.validarFecha
import cl.duoc.agenciaoportunidades.util.validarHorario
import cl.duoc.agenciaoportunidades.util.validarNumeroEntero
import cl.duoc.agenciaoportunidades.util.validarRangoFechas
import cl.duoc.agenciaoportunidades.util.validarTextoObligatorio

// Devuelve un mapa campo -> mensaje de error. Si está vacío, el formulario es válido.
fun validarFormularioOportunidad(
    estado: FormularioOportunidadUiState,
    ahora: Long
): Map<CampoOportunidad, String> {
    val errores = mutableMapOf<CampoOportunidad, String>()

    validarTextoObligatorio(estado.nombre, "El nombre", 5, 100)
        ?.let { errores[CampoOportunidad.NOMBRE] = it }
    validarTextoObligatorio(estado.fuente, "La fuente de información", 3, 60)
        ?.let { errores[CampoOportunidad.FUENTE] = it }
    validarTextoObligatorio(estado.nombreEvento, "El nombre del evento", 5, 100)
        ?.let { errores[CampoOportunidad.EVENTO] = it }
    validarTextoObligatorio(estado.ubicacion, "La ubicación", 3, 100)
        ?.let { errores[CampoOportunidad.UBICACION] = it }
    validarTextoObligatorio(estado.publicoEsperado, "El público esperado", 3, 100)
        ?.let { errores[CampoOportunidad.PUBLICO_ESPERADO] = it }
    validarTextoObligatorio(estado.publicoObjetivo, "El público objetivo", 3, 100)
        ?.let { errores[CampoOportunidad.PUBLICO_OBJETIVO] = it }

    if (estado.organizacionId == null) {
        errores[CampoOportunidad.ORGANIZACION] = "Selecciona una organización"
    }
    if (estado.comunaId == null) {
        errores[CampoOportunidad.COMUNA] = "Selecciona una comuna"
    }

    // ----- Fechas -----
    val errorInicio = validarFecha(estado.fechaInicio, "La fecha de inicio")
    val errorTermino = validarFecha(estado.fechaTermino, "La fecha de término")
    val inicio = parsearFecha(estado.fechaInicio)
    val termino = parsearFecha(estado.fechaTermino)

    if (errorInicio != null) {
        errores[CampoOportunidad.FECHA_INICIO] = errorInicio
    } else if (!estado.esEdicion && inicio != null && diasHasta(inicio, ahora) < 0) {
        errores[CampoOportunidad.FECHA_INICIO] = "La fecha de inicio no puede ser anterior a hoy"
    }

    if (errorTermino != null) {
        errores[CampoOportunidad.FECHA_TERMINO] = errorTermino
    } else {
        validarRangoFechas(inicio, termino)
            ?.let { errores[CampoOportunidad.FECHA_TERMINO] = it }
    }

    validarHorario(estado.horario)
        ?.let { errores[CampoOportunidad.HORARIO] = it }

    // ----- Números (no pueden ser negativos) -----
    validarNumeroEntero(estado.asistentes, "Los asistentes estimados")
        ?.let { errores[CampoOportunidad.ASISTENTES] = it }
    validarNumeroEntero(estado.espacios, "Los espacios disponibles")
        ?.let { errores[CampoOportunidad.ESPACIOS] = it }
    validarNumeroEntero(estado.costo, "El costo referencial")
        ?.let { errores[CampoOportunidad.COSTO] = it }

    return errores
}