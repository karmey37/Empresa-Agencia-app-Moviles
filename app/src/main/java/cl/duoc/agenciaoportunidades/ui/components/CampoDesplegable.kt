package cl.duoc.agenciaoportunidades.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

//lista desplegable: opciones = pares (texto que se muestra, valor que se guarda)
@Composable
fun <T> CampoDesplegable(
    etiqueta: String,
    textoSeleccionado: String,
    opciones: List<Pair<String, T>>,
    onSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textoSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            isError = error != null,
            trailingIcon = {
                if (error != null) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Abrir lista")
                }
            },
            supportingText = {
                if (error != null) {
                    Text(error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        //capa transparente: al tocar el campo se abre el menú
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expandido = true }
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { (texto, valor) ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        onSeleccionar(valor)
                        expandido = false
                    }
                )
            }
        }
    }
}