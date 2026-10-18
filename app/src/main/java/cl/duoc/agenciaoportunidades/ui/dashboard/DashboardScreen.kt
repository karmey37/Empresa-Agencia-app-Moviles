package cl.duoc.agenciaoportunidades.ui.dashboard

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.agenciaoportunidades.model.Usuario
import cl.duoc.agenciaoportunidades.ui.components.Etiqueta
import cl.duoc.agenciaoportunidades.ui.theme.ParDeColor
import cl.duoc.agenciaoportunidades.ui.theme.coloresApp
import cl.duoc.agenciaoportunidades.util.diasHasta
import cl.duoc.agenciaoportunidades.util.esSupervisor
import cl.duoc.agenciaoportunidades.util.etiqueta
import cl.duoc.agenciaoportunidades.util.formatearFecha
import cl.duoc.agenciaoportunidades.util.textoPlazo

private const val MAXIMO_POR_ALERTA = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    usuario: Usuario,
    onAbrirOportunidad: (Int) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuario.id) {
        viewModel.cargar(usuario)
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Inicio") }) },
        bottomBar = bottomBar
    ) { innerPadding ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ContenidoDashboard(
                usuario = usuario,
                uiState = uiState,
                onAbrirOportunidad = onAbrirOportunidad,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ContenidoDashboard(
    usuario: Usuario,
    uiState: DashboardUiState,
    onAbrirOportunidad: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val supervisor = esSupervisor(usuario)
    val colores = MaterialTheme.coloresApp
    val ahora = uiState.ahora

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Hola, ${usuario.nombre}",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = if (supervisor) {
                "${usuario.perfil.etiqueta()} · visión de todo el equipo"
            } else {
                "${usuario.perfil.etiqueta()} · tus oportunidades asignadas"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ----- Indicadores -----
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IndicadorCard(
                valor = uiState.total,
                titulo = if (supervisor) "Oportunidades" else "Mis oportunidades",
                colores = colores.info,
                modifier = Modifier.weight(1f)
            )
            IndicadorCard(
                valor = uiState.abiertas,
                titulo = "Abiertas",
                colores = colores.progreso,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IndicadorCard(
                valor = uiState.confirmadas,
                titulo = "Confirmadas o realizadas",
                colores = colores.exito,
                modifier = Modifier.weight(1f)
            )
            if (supervisor) {
                IndicadorCard(
                    valor = uiState.sinAsignar,
                    titulo = "Sin asignar",
                    colores = colores.alerta,
                    modifier = Modifier.weight(1f)
                )
            } else {
                IndicadorCard(
                    valor = uiState.altaPrioridad,
                    titulo = "Alta prioridad",
                    colores = colores.peligro,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text(
            text = "Alertas",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        // ----- Seguimientos atrasados -----
        SeccionAlerta(
            titulo = "Seguimientos atrasados",
            icono = Icons.Filled.Warning,
            cantidad = uiState.seguimientosAtrasados.size,
            colores = colores.peligro,
            textoVacio = "No hay gestiones atrasadas."
        ) {
            ListaAlerta(items = uiState.seguimientosAtrasados) { seguimiento ->
                val dias = diasHasta(seguimiento.fechaProximoSeguimiento ?: ahora, ahora)
                FilaAlerta(
                    titulo = seguimiento.oportunidadNombre,
                    detalle = "Vencido ${textoPlazo(dias)} · " +
                            seguimiento.proximaAccion.ifBlank { "Seguimiento" },
                    onClick = { onAbrirOportunidad(seguimiento.oportunidadId) }
                )
            }
        }

        // ----- Eventos próximos -----
        SeccionAlerta(
            titulo = "Eventos próximos (30 días)",
            icono = Icons.Filled.Event,
            cantidad = uiState.eventosProximos.size,
            colores = colores.info,
            textoVacio = "No hay eventos en los próximos 30 días."
        ) {
            ListaAlerta(items = uiState.eventosProximos) { evento ->
                FilaAlerta(
                    titulo = evento.nombre,
                    detalle = "Evento ${textoPlazo(diasHasta(evento.fechaInicio, ahora))} · " +
                            "${evento.comuna} · ${formatearFecha(evento.fechaInicio)}",
                    onClick = { onAbrirOportunidad(evento.id) }
                )
            }
        }

        // ----- Seguimientos pendientes -----
        SeccionAlerta(
            titulo = "Seguimientos pendientes",
            icono = Icons.Filled.Schedule,
            cantidad = uiState.seguimientosPendientes.size,
            colores = colores.advertencia,
            textoVacio = "No hay seguimientos programados."
        ) {
            ListaAlerta(items = uiState.seguimientosPendientes) { seguimiento ->
                val dias = diasHasta(seguimiento.fechaProximoSeguimiento ?: ahora, ahora)
                FilaAlerta(
                    titulo = seguimiento.oportunidadNombre,
                    detalle = "Seguimiento ${textoPlazo(dias)} · " +
                            seguimiento.proximaAccion.ifBlank { "Sin acción definida" },
                    onClick = { onAbrirOportunidad(seguimiento.oportunidadId) }
                )
            }
        }

        // ----- Sin seguimiento -----
        SeccionAlerta(
            titulo = "Oportunidades sin seguimiento",
            icono = Icons.Filled.Info,
            cantidad = uiState.sinSeguimiento.size,
            colores = colores.alerta,
            textoVacio = "Todas las oportunidades tienen gestiones registradas."
        ) {
            ListaAlerta(items = uiState.sinSeguimiento) { oportunidad ->
                FilaAlerta(
                    titulo = oportunidad.nombre,
                    detalle = "Aún no tiene gestiones registradas",
                    onClick = { onAbrirOportunidad(oportunidad.id) }
                )
            }
        }
    }
}

@Composable
private fun IndicadorCard(
    valor: Int,
    titulo: String,
    colores: ParDeColor,
    modifier: Modifier = Modifier
) {
    // ANIMACIÓN: el número sube desde 0 hasta su valor al abrir el dashboard
    var objetivo by remember { mutableIntStateOf(0) }
    LaunchedEffect(valor) { objetivo = valor }
    val valorAnimado by animateIntAsState(
        targetValue = objetivo,
        animationSpec = tween(durationMillis = 800),
        label = "indicador"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colores.fondo,
            contentColor = colores.texto
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valorAnimado.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SeccionAlerta(
    titulo: String,
    icono: ImageVector,
    cantidad: Int,
    colores: ParDeColor,
    textoVacio: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Etiqueta(texto = cantidad.toString(), colores = colores)
            }
            if (cantidad == 0) {
                Text(
                    text = textoVacio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                contenido()
            }
        }
    }
}

// Muestra los primeros elementos de la lista, separados por una línea
@Composable
private fun <T> ListaAlerta(
    items: List<T>,
    contenidoItem: @Composable (T) -> Unit
) {
    items.take(MAXIMO_POR_ALERTA).forEachIndexed { indice, item ->
        if (indice > 0) HorizontalDivider()
        contenidoItem(item)
    }
    if (items.size > MAXIMO_POR_ALERTA) {
        Text(
            text = "y ${items.size - MAXIMO_POR_ALERTA} más (revísalas en Oportunidades)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FilaAlerta(
    titulo: String,
    detalle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(text = titulo, style = MaterialTheme.typography.titleSmall)
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}