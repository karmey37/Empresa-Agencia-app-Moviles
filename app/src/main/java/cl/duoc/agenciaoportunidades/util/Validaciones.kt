package cl.duoc.agenciaoportunidades.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

// Todas las funciones devuelven el mensaje de error, o null si el dato es válido

private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val REGEX_TELEFONO = Regex("^(\\+56)?[0-9]{9}$")
private val REGEX_FECHA = Regex("^[0-9]{2}/[0-9]{2}/[0-9]{4}$")

fun validarCorreo(correo: String): String? {
    val valor = correo.trim()
    return when {
        valor.isEmpty() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(valor) -> "Ingresa un correo válido (ej: nombre@dominio.cl)"
        else -> null
    }
}

fun validarPassword(password: String): String? {
    return when {
        password.isEmpty() -> "La contraseña es obligatoria"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }
}

fun validarTextoObligatorio(valor: String, campo: String, minimo: Int = 3, maximo: Int = 100): String? {
    val texto = valor.trim()
    return when {
        texto.isEmpty() -> "$campo es obligatorio"
        texto.length < minimo -> "$campo debe tener al menos $minimo caracteres"
        texto.length > maximo -> "$campo no puede superar los $maximo caracteres"
        else -> null
    }
}

fun validarTelefono(telefono: String): String? {
    val valor = telefono.replace(" ", "")
    return when {
        valor.isEmpty() -> "El teléfono es obligatorio"
        !REGEX_TELEFONO.matches(valor) -> "Debe tener 9 dígitos (ej: 912345678)"
        else -> null
    }
}

// Convierte "dd/MM/yyyy" a milisegundos; devuelve null si la fecha no existe
fun parsearFecha(texto: String): Long? {
    val valor = texto.trim()
    if (!REGEX_FECHA.matches(valor)) return null
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    formato.isLenient = false
    return try {
        formato.parse(valor)?.time
    } catch (e: ParseException) {
        null
    }
}

fun validarFecha(texto: String, campo: String = "La fecha"): String? {
    return when {
        texto.isBlank() -> "$campo es obligatoria"
        parsearFecha(texto) == null -> "$campo no es válida (formato dd/mm/aaaa)"
        else -> null
    }
}

fun validarRangoFechas(inicio: Long?, termino: Long?): String? {
    return if (inicio != null && termino != null && termino < inicio) {
        "La fecha de término no puede ser anterior a la de inicio"
    } else {
        null
    }
}

fun validarNumeroEntero(texto: String, campo: String, minimo: Int = 0): String? {
    val numero = texto.trim().toIntOrNull()
    return when {
        texto.isBlank() -> "$campo es obligatorio"
        numero == null -> "$campo debe ser un número entero"
        numero < minimo && minimo == 0 -> "$campo no puede ser negativo"
        numero < minimo -> "$campo no puede ser menor que $minimo"
        else -> null
    }
}

private val REGEX_HORARIO = Regex("^([01][0-9]|2[0-3]):[0-5][0-9] - ([01][0-9]|2[0-3]):[0-5][0-9]$")

fun validarHorario(horario: String): String? {
    val valor = horario.trim()
    return when {
        valor.isEmpty() -> "El horario es obligatorio"
        !REGEX_HORARIO.matches(valor) -> "Usa el formato HH:mm - HH:mm (ej: 10:00 - 16:00)"
        else -> {
            val partes = valor.split(" - ")
            val inicio = partes[0].replace(":", "").toInt()
            val fin = partes[1].replace(":", "").toInt()
            if (fin <= inicio) "La hora de término debe ser posterior a la de inicio" else null
        }
    }
}

private val REGEX_SITIO_WEB = Regex("^https?://[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(/.*)?$")

// El sitio web es opcional, pero si se escribe debe ser una dirección valida
fun validarSitioWebOpcional(sitio: String): String? {
    val valor = sitio.trim()
    return when {
        valor.isEmpty() -> null
        !REGEX_SITIO_WEB.matches(valor) -> "Usa una dirección válida (ej: https://www.ejemplo.cl)"
        else -> null
    }
}