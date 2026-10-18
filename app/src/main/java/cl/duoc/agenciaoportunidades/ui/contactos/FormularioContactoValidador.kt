package cl.duoc.agenciaoportunidades.ui.contactos

import cl.duoc.agenciaoportunidades.util.validarCorreo
import cl.duoc.agenciaoportunidades.util.validarTelefono
import cl.duoc.agenciaoportunidades.util.validarTextoObligatorio

fun validarFormularioContacto(
    estado: FormularioContactoUiState
): Map<CampoContacto, String> {
    val errores = mutableMapOf<CampoContacto, String>()

    validarTextoObligatorio(estado.nombre, "El nombre", 3, 80)
        ?.let { errores[CampoContacto.NOMBRE] = it }
    validarTextoObligatorio(estado.cargo, "El cargo o función", 3, 60)
        ?.let { errores[CampoContacto.CARGO] = it }
    validarCorreo(estado.correo)
        ?.let { errores[CampoContacto.CORREO] = it }
    validarTelefono(estado.telefono)
        ?.let { errores[CampoContacto.TELEFONO] = it }

    return errores
}