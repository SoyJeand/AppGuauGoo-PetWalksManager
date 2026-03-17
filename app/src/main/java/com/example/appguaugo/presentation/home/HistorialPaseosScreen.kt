package com.example.appguaugo.presentation.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appguaugo.R
import com.example.appguaugo.ui.theme.GuauYellow
import java.text.SimpleDateFormat
import java.util.*

// --- PASO 1: DEFINIMOS UNA CLASE DE DATOS SIMULADA ---
// Esto nos independiza de la entidad real de Room por ahora.
data class PaseoEjemplo(
    val id: Long,
    val mascotaNombre: String,
    val estado: String,
    val fecha: Date,
    val tipoPaseo: String,
    val costoOfrecido: Double,
    val origen: String
)

// --- PASO 2: CREAMOS UNA LISTA CON DATOS DE EJEMPLO ---
val sampleWalkHistory = listOf(
    PaseoEjemplo(
        id = 1,
        mascotaNombre = "Rocky",
        estado = "Completado",
        fecha = Date(System.currentTimeMillis() - 86400000L * 1), // Ayer
        tipoPaseo = "Normal (1 h)",
        costoOfrecido = 25.50,
        origen = "Parque Kennedy, Miraflores"
    ),
    PaseoEjemplo(
        id = 2,
        mascotaNombre = "Luna",
        estado = "Cancelado",
        fecha = Date(System.currentTimeMillis() - 86400000L * 3), // Hace 3 días
        tipoPaseo = "Corto (30 min)",
        costoOfrecido = 15.00,
        origen = "Av. Arequipa 520, Lima"
    ),
    PaseoEjemplo(
        id = 3,
        mascotaNombre = "Max",
        estado = "Aceptado",
        fecha = Date(System.currentTimeMillis() - 3600000L * 2), // Hace 2 horas
        tipoPaseo = "Extendido (2h)",
        costoOfrecido = 40.00,
        origen = "Malecón de la Reserva"
    ),
    PaseoEjemplo(
        id = 4,
        mascotaNombre = "Rocky",
        estado = "Pendiente",
        fecha = Date(), // Ahora mismo
        tipoPaseo = "Normal (1 h)",
        costoOfrecido = 28.00,
        origen = "Óvalo de Miraflores"
    )
)

// --- PASO 3: PANTALLA PRINCIPAL (SIN VIEWMODEL) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkHistoryScreen(
    onNavigateBack: () -> Unit,
    historialDePaseos: List<PaseoEjemplo> // La pantalla recibe la lista directamente
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Paseos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GuauYellow,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            if (historialDePaseos.isEmpty()) {
                EmptyHistoryView() // Vista para cuando no hay datos
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(historialDePaseos, key = { it.id }) { paseo ->
                        WalkHistoryCard(paseo = paseo)
                    }
                }
            }
        }
    }
}

// --- PASO 4: LA TARJETA Y LOS COMPONENTES DE UI (SIN CAMBIOS) ---
// Estos componentes ya están bien, solo adaptamos el tipo de dato que reciben.

@Composable
fun WalkHistoryCard(paseo: PaseoEjemplo) {
    val statusColor = when (paseo.estado) {
        "Completado" -> Color(0xFF388E3C) // Verde
        "Cancelado" -> Color(0xFFD32F2F) // Rojo
        "Aceptado" -> Color(0xFF1976D2) // Azul
        else -> Color.Gray // Pendiente
    }
    val statusIcon = when (paseo.estado) {
        "Completado" -> Icons.Default.CheckCircle
        "Cancelado" -> Icons.Default.Cancel
        "Aceptado" -> Icons.Default.ThumbUp
        else -> Icons.Default.HourglassEmpty
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paseo para ${paseo.mascotaNombre}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(statusIcon, contentDescription = "Estado", tint = statusColor, modifier = Modifier.size(16.dp))
                    Text(paseo.estado, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(icon = Icons.Default.CalendarToday, text = formatFecha(paseo.fecha))
            InfoRow(icon = Icons.Default.WatchLater, text = paseo.tipoPaseo)
            InfoRow(icon = Icons.Default.AttachMoney, text = "S/ ${"%.2f".format(paseo.costoOfrecido)}")
            InfoRow(icon = Icons.Default.LocationOn, text = "Desde: ${paseo.origen}")
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun EmptyHistoryView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.dog_walking_icon),
            contentDescription = "No hay historial",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Aún no has solicitado paseos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Todas tus solicitudes de paseo aparecerán aquí una vez que las realices.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatFecha(date: Date): String {
    val format = SimpleDateFormat("dd 'de' MMMM, yyyy 'a las' hh:mm a", Locale("es", "ES"))
    return format.format(date)
}

// --- PASO 5: PREVIEWS PARA VERIFICAR EL DISEÑO ---
@Preview(showBackground = true, name = "Historial Lleno")
@Composable
fun WalkHistoryScreenPreview() {
    MaterialTheme {
        WalkHistoryScreen(
            onNavigateBack = {},
            historialDePaseos = sampleWalkHistory // Le pasamos los datos de ejemplo
        )
    }
}

@Preview(showBackground = true, name = "Historial Vacío")
@Composable
fun WalkHistoryScreenEmptyPreview() {
    MaterialTheme {
        WalkHistoryScreen(
            onNavigateBack = {},
            historialDePaseos = emptyList() // Le pasamos una lista vacía
        )
    }
}

