package com.example.appguaugo.presentation.home

import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.viewmodel.RequestListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RequestListScreen(
    viewModel: RequestListViewModel
) {
    val requests by viewModel.requests.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(requests) { solicitud ->
            SolicitudCard(
                solicitud = solicitud,
                onAceptarOferta = { paseadorId, monto ->
                    viewModel.aceptarContraoferta(solicitud.id, paseadorId, monto)
                }
            )
        }
    }
}

@Composable
fun SolicitudCard(
    solicitud: SolicitudPaseoEntity,
    onAceptarOferta: (Int, Double) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- CABECERA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = solicitud.mascotaNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = dateFormat.format(solicitud.fechaSolicitud), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                StatusChip(status = solicitud.estado)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- DETALLES ---
            Text("De: ${solicitud.origen}")
            Text("A: ${solicitud.destino}")
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tu oferta: ", fontWeight = FontWeight.SemiBold)
                Text("S/ ${solicitud.costoOfrecido}")
            }

            // --- SECCIÓN DE CONTRAOFERTAS ---
            // Solo mostramos esto si estamos BUSCANDO y hay ofertas en el mapa
            if (solicitud.estado == "BUSCANDO" && solicitud.contraofertas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Contraofertas recibidas:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Iteramos sobre el mapa de contraofertas
                solicitud.contraofertas.forEach { (paseadorId, monto) ->
                    ContraofertaItem(
                        paseadorId = paseadorId,
                        monto = monto,
                        onAceptar = { onAceptarOferta(paseadorId, monto) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (solicitud.estado == "CONFIRMADO") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Paseo acordado por S/ ${solicitud.precioFinal}",
                    color = Color(0xFF2E7D32), // Verde oscuro
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ContraofertaItem(
    paseadorId: Int,
    monto: Double,
    onAceptar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)) // Azul muy claro
            .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Paseador #$paseadorId", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Ofrece: S/ $monto", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onAceptar,
            modifier = Modifier.height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Text("Aceptar", fontSize = 12.sp)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when(status) {
        "BUSCANDO" -> Color.Gray
        "CONFIRMADO" -> Color(0xFF4CAF50) // Verde
        "FINALIZADO" -> Color(0xFF2196F3) // Azul
        else -> Color.Black
    }

    Text(
        text = status,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
