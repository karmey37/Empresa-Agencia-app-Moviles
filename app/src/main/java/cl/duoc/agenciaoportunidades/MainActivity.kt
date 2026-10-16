package cl.duoc.agenciaoportunidades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.agenciaoportunidades.ui.navigation.Navegacion
import cl.duoc.agenciaoportunidades.ui.theme.AgenciaOportunidadesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgenciaOportunidadesTheme {
                Navegacion()
            }
        }
    }
}