package com.example.appguaugo.presentation.home

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appguaugo.ui.theme.GuauYellowDark

// Definimos los posibles estados de verificación de un documento
enum class DocumentStatus {
    PENDING,    // El usuario debe seleccionar el archivo
    UPLOADING,  // Archivo seleccionado, en proceso de revisión (simula el estado "verificando")
    APPROVED,   // Aprobado
    REJECTED    // Rechazado, requiere nueva subida
}

// Estructura de datos para un documento
data class Document(
    val title: String,
    val description: String,
    val status: DocumentStatus = DocumentStatus.PENDING,
    val uri: Uri? = null, // URI del archivo seleccionado
    val errorMessage: String? = null // Mensaje de rechazo si aplica
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // --- 1. ESTADOS LOCALES PARA LAS 3 FOTOS REQUERIDAS ---
    var dniFrontDocument by remember {
        mutableStateOf(
            Document(
                title = "1. DNI / Cédula Frontal",
                description = "Sube una foto clara y legible de la parte frontal de tu documento.",
            )
        )
    }

    var dniBackDocument by remember {
        mutableStateOf(
            Document(
                title = "2. DNI / Cédula Trasera",
                description = "Sube una foto clara y legible de la parte trasera de tu documento.",
            )
        )
    }

    var selfieDocument by remember {
        mutableStateOf(
            Document(
                title = "3. Selfie con DNI en mano",
                description = "Tómate una selfie sosteniendo tu DNI o cédula junto a tu rostro.",
            )
        )
    }

    // Comprobación de si todos los documentos requeridos han sido subidos al menos una vez
    val allRequiredDocumentsSelected by remember {
        derivedStateOf {
            dniFrontDocument.uri != null && dniBackDocument.uri != null && selfieDocument.uri != null
        }
    }

    // --- 2. LAUNCHERS DE ARCHIVOS ---
    // Launcher para DNI Frontal
    val dniFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            dniFrontDocument = dniFrontDocument.copy(
                status = DocumentStatus.PENDING, // PENDING antes de la subida, luego pasa a UPLOADING
                uri = uri,
                errorMessage = null
            )
            Toast.makeText(context, "DNI Frontal seleccionado.", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para DNI Trasero
    val dniBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            dniBackDocument = dniBackDocument.copy(
                status = DocumentStatus.PENDING,
                uri = uri,
                errorMessage = null
            )
            Toast.makeText(context, "DNI Trasero seleccionado.", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para Selfie con DNI
    val selfieLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selfieDocument = selfieDocument.copy(
                status = DocumentStatus.PENDING,
                uri = uri,
                errorMessage = null
            )
            Toast.makeText(context, "Selfie con DNI seleccionada.", Toast.LENGTH_SHORT).show()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificación de Datos", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Banner de Información General ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Información", tint = GuauYellowDark)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "La verificación es obligatoria para tu seguridad y la confianza de los clientes. Sube las 3 fotos requeridas para iniciar el proceso.",
                        fontSize = 14.sp
                    )
                }
            }

            Text(
                text = "Documentos de Identidad (3 fotos)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // --- 3. COMPONENTES DE SUBIDA DE ARCHIVOS ---
            // DNI Frontal
            DocumentUploadCard(
                document = dniFrontDocument,
                onClick = { dniFrontLauncher.launch("image/*") }
            )

            // DNI Trasero
            DocumentUploadCard(
                document = dniBackDocument,
                onClick = { dniBackLauncher.launch("image/*") }
            )

            // Selfie con DNI en mano
            DocumentUploadCard(
                document = selfieDocument,
                onClick = { selfieLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. BOTÓN DE ENVIAR A REVISIÓN ---
            Button(
                onClick = {
                    // Lógica para cambiar los 3 estados a 'UPLOADING' (Verificando)
                    dniFrontDocument = dniFrontDocument.copy(status = DocumentStatus.UPLOADING)
                    dniBackDocument = dniBackDocument.copy(status = DocumentStatus.UPLOADING)
                    selfieDocument = selfieDocument.copy(status = DocumentStatus.UPLOADING)

                    Toast.makeText(context, "Documentos enviados a revisión. Estado: Verificando.", Toast.LENGTH_LONG).show()
                    // Aquí se llamaría al ViewModel para enviar las 3 URIs al backend/DB
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = allRequiredDocumentsSelected && dniFrontDocument.status != DocumentStatus.APPROVED, // Habilitar solo si las 3 están seleccionadas Y no están Aprobadas
                colors = ButtonDefaults.buttonColors(containerColor = GuauYellowDark)
            ) {
                Text(
                    text = when {
                        dniFrontDocument.status == DocumentStatus.APPROVED -> "¡Verificación Completa!"
                        !allRequiredDocumentsSelected -> "Faltan documentos por adjuntar"
                        else -> "Enviar a Revisión"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

// --- COMPONENTE REUTILIZABLE PARA SUBIDA DE DOCUMENTOS ---
@Composable
fun DocumentUploadCard(
    document: Document,
    onClick: () -> Unit
) {
    val statusColor = when (document.status) {
        DocumentStatus.APPROVED -> Color(0xFF4CAF50) // Verde
        DocumentStatus.REJECTED -> Color(0xFFF44336) // Rojo
        DocumentStatus.UPLOADING -> Color(0xFFFF9800) // Naranja (Verificando)
        DocumentStatus.PENDING -> if (document.uri != null) Color(0xFF2196F3) else Color.Gray // Azul si hay URI, Gris si no
    }
    val statusIcon = when (document.status) {
        DocumentStatus.APPROVED -> Icons.Default.AssignmentTurnedIn
        DocumentStatus.REJECTED -> Icons.Default.Error
        DocumentStatus.UPLOADING -> Icons.Default.Info
        DocumentStatus.PENDING -> Icons.Default.CloudUpload
    }
    val statusText = when (document.status) {
        DocumentStatus.APPROVED -> "APROBADO"
        DocumentStatus.REJECTED -> "RECHAZADO"
        DocumentStatus.UPLOADING -> "VERIFICANDO"
        DocumentStatus.PENDING -> if (document.uri != null) "ADJUNTADO" else "SUBIR FOTO"
    }
    val clickEnabled = document.status != DocumentStatus.UPLOADING && document.status != DocumentStatus.APPROVED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickEnabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(1.dp, statusColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Título y descripción
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = document.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (document.status == DocumentStatus.REJECTED && document.errorMessage != null) {
                    Text(
                        text = "Razón: ${document.errorMessage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Indicador de acción o estado
            Text(
                text = statusText,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
        }
    }
}