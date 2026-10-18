package cl.duoc.agenciaoportunidades.ui.oportunidad

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun CamaraOportunidadScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) imagenUri = uriPendiente
        uriPendiente = null
    }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Button(onClick = {
            val carpeta = File(context.cacheDir, "fotos")
            carpeta.mkdirs()
            val archivo = File.createTempFile("foto_", ".jpg", carpeta)

            // Reemplaza "cl.duoc.agenciaoportunidades.fileprovider" por el nombre exacto de tu provider en el Manifest
            val nuevaUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )
            uriPendiente = nuevaUri
            camaraLauncher.launch(nuevaUri)
        }) {
            Text("Fotografiar Evento")
        }

        if (imagenUri != null) {
            AsyncImage(
                model = imagenUri,
                contentDescription = "Evidencia del evento",
                modifier = Modifier.fillMaxWidth().height(250.dp).padding(top = 16.dp)
            )
        }
    }
}