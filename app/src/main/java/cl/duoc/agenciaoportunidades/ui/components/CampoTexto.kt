package cl.duoc.agenciaoportunidades.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

// Campo de texto con mensaje de error debajo, ícono de error y contador de caracteres
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLength: Int? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo ->
            if (maxLength == null || nuevo.length <= maxLength) {
                onValorChange(nuevo)
            }
        },
        label = { Text(etiqueta) },
        isError = error != null,
        trailingIcon = {
            if (error != null) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        supportingText = {
            if (error != null) {
                Text(error)
            } else if (maxLength != null) {
                Text("${valor.length}/$maxLength")
            }
        },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}