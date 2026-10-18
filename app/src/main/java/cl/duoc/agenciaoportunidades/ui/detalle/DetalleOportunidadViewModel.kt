package cl.duoc.agenciaoportunidades.ui.detalle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.agenciaoportunidades.data.AgenciaRepository
import cl.duoc.agenciaoportunidades.data.AppDatabase
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Evidencia
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.util.estadosPermitidos
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.puedeAsignarResponsable
import cl.duoc.agenciaoportunidades.util.puedeEditarOportunidad
import cl.duoc.agenciaoportunidades.util.puedeVerOportunidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class DetalleOportunidadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgenciaRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    private var usuario: Usuario? = null
    private var oportunidadId: Int? = null

    fun cargar(id: Int, usuarioActual: Usuario) {
        if (oportunidadId == id) return
        oportunidadId = id
        usuario = usuarioActual

        viewModelScope.launch {
            repository.observarDetalleOportunidad(id).collect { detalle ->
                // Un ejecutivo no puede abrir oportunidades asignadas a otra persona
                val sinAcceso = detalle != null &&
                        !puedeVerOportunidad(usuarioActual, detalle.responsableId)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        detalle = if (sinAcceso) null else detalle,
                        sinAcceso = sinAcceso
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.obtenerSeguimientos(id).collect { lista ->
                _uiState.update { it.copy(seguimientos = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerHistorialEstados(id).collect { lista ->
                _uiState.update { it.copy(historialEstados = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerEvidencias(id).collect { lista ->
                _uiState.update { it.copy(evidencias = lista) }
            }
        }
        viewModelScope.launch {
            repository.obtenerEjecutivos().collect { lista ->
                _uiState.update { it.copy(ejecutivos = lista) }
            }
        }
    }

    // ================= Estado y responsable =================

    fun cambiarEstado(nuevoEstado: EstadoOportunidad, observacion: String) {
        val usuarioActual = usuario ?: return
        val id = oportunidadId ?: return

        viewModelScope.launch {
            try {
                val oportunidad = repository.obtenerOportunidad(id)
                if (oportunidad == null) {
                    avisar("No se encontró la oportunidad")
                    return@launch
                }
                if (nuevoEstado !in estadosPermitidos(usuarioActual, oportunidad.estado)) {
                    avisar("No tienes permiso para realizar este cambio de estado")
                    return@launch
                }
                repository.cambiarEstado(
                    oportunidad = oportunidad,
                    nuevoEstado = nuevoEstado,
                    usuarioId = usuarioActual.id,
                    observaciones = observacion.trim()
                )
                avisar("Estado actualizado a \"${nuevoEstado.etiqueta()}\"")
            } catch (e: Exception) {
                avisar("No se pudo actualizar el estado. Inténtalo nuevamente")
            }
        }
    }

    // responsableId null = dejar la oportunidad sin asignar
    fun asignarResponsable(responsableId: Int?) {
        val usuarioActual = usuario ?: return
        val id = oportunidadId ?: return

        if (!puedeAsignarResponsable(usuarioActual)) {
            avisar("Solo el supervisor puede asignar responsables")
            return
        }

        viewModelScope.launch {
            try {
                val oportunidad = repository.obtenerOportunidad(id)
                if (oportunidad == null) {
                    avisar("No se encontró la oportunidad")
                    return@launch
                }
                repository.asignarResponsable(oportunidad, responsableId)
                avisar(
                    if (responsableId == null) "La oportunidad quedó sin asignar"
                    else "Responsable asignado correctamente"
                )
            } catch (e: Exception) {
                avisar("No se pudo asignar el responsable. Inténtalo nuevamente")
            }
        }
    }

    // ================= Evidencias fotográficas =================

    // Crea (vacío) el archivo donde la cámara va a guardar la foto, dentro de la carpeta privada de la app
    fun crearArchivoEvidencia(): File {
        val carpeta = File(getApplication<Application>().filesDir, "evidencias")
        if (!carpeta.exists()) carpeta.mkdirs()
        return File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
    }

    // La cámara terminó bien: la foto queda asociada a la oportunidad
    fun registrarEvidencia(ruta: String) {
        val id = oportunidadId ?: return
        if (!puedeModificar()) {
            File(ruta).delete()
            avisar("No tienes permiso para agregar evidencias a esta oportunidad")
            return
        }

        viewModelScope.launch {
            try {
                val archivo = File(ruta)
                if (!archivo.exists() || archivo.length() == 0L) {
                    archivo.delete()
                    avisar("No se pudo guardar la fotografía")
                    return@launch
                }
                repository.guardarEvidencia(Evidencia(oportunidadId = id, rutaArchivo = ruta))
                avisar("Fotografía guardada correctamente")
            } catch (e: Exception) {
                File(ruta).delete()
                avisar("No se pudo guardar la fotografía. Inténtalo nuevamente")
            }
        }
    }

    // El usuario cerró la cámara sin tomar la foto: se borra el archivo vacío
    fun cancelarCaptura(ruta: String?) {
        ruta?.let { File(it).delete() }
        avisar("Captura cancelada")
    }

    // Copia una imagen elegida de la galería a la carpeta de la app
    fun importarDesdeGaleria(uri: Uri) {
        val id = oportunidadId ?: return
        if (!puedeModificar()) {
            avisar("No tienes permiso para agregar evidencias a esta oportunidad")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val destino = crearArchivoEvidencia()
            try {
                val entrada = getApplication<Application>().contentResolver.openInputStream(uri)
                    ?: throw IOException("No se pudo leer la imagen")
                entrada.use { origen ->
                    destino.outputStream().use { salida -> origen.copyTo(salida) }
                }
                repository.guardarEvidencia(
                    Evidencia(oportunidadId = id, rutaArchivo = destino.absolutePath)
                )
                avisar("Fotografía agregada correctamente")
            } catch (e: Exception) {
                destino.delete()
                avisar("No se pudo importar la imagen. Inténtalo nuevamente")
            }
        }
    }

    fun eliminarEvidencia(evidencia: Evidencia) {
        if (!puedeModificar()) {
            avisar("No tienes permiso para eliminar evidencias de esta oportunidad")
            return
        }

        viewModelScope.launch {
            try {
                repository.eliminarEvidencia(evidencia)
                File(evidencia.rutaArchivo).delete()
                avisar("Fotografía eliminada")
            } catch (e: Exception) {
                avisar("No se pudo eliminar la fotografía")
            }
        }
    }

    // ================= Mensajes =================

    fun avisar(texto: String) {
        _uiState.update { it.copy(mensaje = texto) }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    // Solo el supervisor, o el responsable mientras la oportunidad siga abierta, puede modificarla
    private fun puedeModificar(): Boolean {
        val usuarioActual = usuario ?: return false
        val detalle = _uiState.value.detalle ?: return false
        return puedeEditarOportunidad(usuarioActual, detalle.estado)
    }
}