package cl.duoc.agenciaoportunidades.ui.oportunidad

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun BotonAgendarCalendario(titulo: String, fechaInicioMs: Long, fechaFinMs: Long) {
    val context = LocalContext.current

    Button(onClick = {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fechaInicioMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fechaFinMs)
        }
        context.startActivity(intent)
    }) {
        Text("Agendar en Calendario")
    }
}