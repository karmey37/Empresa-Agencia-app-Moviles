package cl.duoc.agenciaoportunidades.ui.formulario

import cl.duoc.agenciaoportunidades.model.Comuna
import cl.duoc.agenciaoportunidades.model.Organizacion
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.TipoEvento
import cl.duoc.agenciaoportunidades.model.Usuario

// Cada campo que puede mostrar un error
enum class CampoOportunidad {
    NOMBRE, FUENTE, ORGANIZACION, COMUNA, EVENTO,
    FECHA_INICIO, FECHA_TERMINO, HORARIO, UBICACION,
    PUBLICO_ESPERADO, PUBLICO_OBJETIVO, ASISTENTES, ESPACIOS, COSTO
}

data class FormularioOportunidadUiState(
    // ----- Estado de la pantalla -----
    val cargando: Boolean = true,
    val esEdicion: Boolean = false,
    val sinPermiso: Boolean = false,
    val noEncontrada: Boolean = false,
    val guardando: Boolean = false,
    val guardadoId: Int? = null,
    val mensajeError: String? = null,

    // ----- Datos para las listas desplegables -----
    val organizaciones: List<Organizacion> = emptyList(),
    val comunas: List<Comuna> = emptyList(),
    val ejecutivos: List<Usuario> = emptyList(),

    // ----- Valores ingresados (se conservan si falla una validación) -----
    val nombre: String = "",
    val descripcion: String = "",
    val fuente: String = "",
    val responsableId: Int? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val potencial: Prioridad = Prioridad.MEDIA,
    val afinidad: Prioridad = Prioridad.MEDIA,
    val organizacionId: Int? = null,
    val nombreEvento: String = "",
    val tipoEvento: TipoEvento = TipoEvento.EVENTO_CORPORATIVO,
    val comunaId: Int? = null,
    val ubicacion: String = "",
    val fechaInicio: String = "",
    val fechaTermino: String = "",
    val horario: String = "",
    val publicoEsperado: String = "",
    val publicoObjetivo: String = "",
    val asistentes: String = "",
    val espacios: String = "",
    val costo: String = "0",
    val condiciones: String = "",
    val requisitos: String = "",
    val observaciones: String = "",

    // ----- Errores por campo (si el campo no está en el mapa, no tiene error) -----
    val errores: Map<CampoOportunidad, String> = emptyMap()
)