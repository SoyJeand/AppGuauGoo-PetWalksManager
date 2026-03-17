package com.example.appguaugo.presentation.walker_home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appguaugo.application.GuauApp
import com.example.appguaugo.data.entity.PaseadorVerificacionEntity
import com.example.appguaugo.data.entity.VerificationStatus
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// --- 1. LÓGICA Y ESTADOS (DE TU PANTALLA ORIGINAL) ---

// Definimos los posibles estados de una subida individual
enum class DocumentUploadStatus {
    MISSING,    // El usuario debe seleccionar el archivo
    SELECTED,   // Archivo seleccionado, listo para enviar
}

// Estructura de datos para un documento a subir
data class DocumentToUpload(
    val title: String,
    val description: String,
    val status: DocumentUploadStatus = DocumentUploadStatus.MISSING,
    val uri: Uri? = null, // URI del archivo seleccionado
)


// --- 2. VIEWMODEL Y FACTORY (DE MI PROPUESTA) ---

class VerificationViewModel(private val repository: ClienteRepository) : ViewModel() {
    fun getVerificationState(userId: Int): Flow<PaseadorVerificacionEntity?> {
        return repository.getVerificacion(userId)
    }

    fun submitForVerification(
        userId: Int,
        uri1: Uri,
        uri2: Uri,
        uri3: Uri,
        onResult: (success: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val verificationData = PaseadorVerificacionEntity(
                    paseadorId = userId,
                    // Guardamos la URI como String en la base de datos
                    fotoDocumentoFrenteUri = uri1.toString(),
                    fotoDocumentoReversoUri = uri2.toString(),
                    fotoSelfieConDocumentoUri = uri3.toString(),
                    estado = VerificationStatus.PENDIENTE
                )
                repository.guardarVerificacion(verificationData)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}

class VerificationViewModelFactory(
    private val repository: ClienteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VerificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- 3. PANTALLA PRINCIPAL (FUSIONADA) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreenn(onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    val loggedInUserId = remember {
        context.getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)
    }

    val repository = remember {
        ClienteRepository(
            GuauApp.db.clienteDao(), GuauApp.db.mascotaDao(), GuauApp.db.solicitudPaseoDao(),
            GuauApp.db.reclamoDao(), GuauApp.db.paseadorVerificacionDao()
        )
    }

    val viewModel: VerificationViewModel = viewModel(factory = VerificationViewModelFactory(repository))

    val verificationState by viewModel.getVerificationState(loggedInUserId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificación de Identidad") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver atrás") } }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = verificationState,
            modifier = Modifier.padding(innerPadding),
            label = "verification_state_animation"
        ) { state ->
            when (state?.estado) {
                VerificationStatus.PENDIENTE -> {
                    StatusView(
                        icon = Icons.Default.HourglassTop, title = "En Revisión",
                        message = "Hemos recibido tus documentos. Nuestro equipo los revisará en las próximas 24-48 horas.",
                        iconColor = Color(0xFFE65100) // Naranja
                    )
                }
                VerificationStatus.VERIFICADO -> {
                    StatusView(
                        icon = Icons.Default.VerifiedUser, title = "¡Cuenta Verificada!",
                        message = "Felicidades, tu identidad ha sido verificada. Ya tienes acceso completo a las funciones para paseadores.",
                        iconColor = Color(0xFF388E3C) // Verde
                    )
                }
                else -> { // Para 'NO_VERIFICADO' o nulo, muestra el formulario
                    VerificationForm(viewModel = viewModel, userId = loggedInUserId)
                }
            }
        }
    }
}

// --- 4. COMPOSABLE DEL FORMULARIO (FUSIONADO) ---

@Composable
private fun VerificationForm(viewModel: VerificationViewModel, userId: Int) {
    val context = LocalContext.current

    // --- Estados locales para las 3 fotos, usando tu `DocumentToUpload` ---
    var dniFrontDocument by remember { mutableStateOf(DocumentToUpload("1. DNI / Cédula Frontal", "Foto clara de la parte frontal.")) }
    var dniBackDocument by remember { mutableStateOf(DocumentToUpload("2. DNI / Cédula Trasera", "Foto clara de la parte trasera.")) }
    var selfieDocument by remember { mutableStateOf(DocumentToUpload("3. Selfie con DNI", "Sostén tu DNI junto a tu rostro.")) }

    // Comprobación de si todos los documentos han sido seleccionados
    val allDocumentsSelected by remember {
        derivedStateOf {
            dniFrontDocument.uri != null && dniBackDocument.uri != null && selfieDocument.uri != null
        }
    }

    // --- Launchers reales para abrir la galería ---
    val dniFrontLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            dniFrontDocument = dniFrontDocument.copy(status = DocumentUploadStatus.SELECTED, uri = uri)
            Toast.makeText(context, "DNI Frontal seleccionado.", Toast.LENGTH_SHORT).show()
        }
    }
    val dniBackLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            dniBackDocument = dniBackDocument.copy(status = DocumentUploadStatus.SELECTED, uri = uri)
            Toast.makeText(context, "DNI Trasero seleccionado.", Toast.LENGTH_SHORT).show()
        }
    }
    val selfieLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selfieDocument = selfieDocument.copy(status = DocumentUploadStatus.SELECTED, uri = uri)
            Toast.makeText(context, "Selfie seleccionada.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Verifica tu Identidad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Para garantizar la seguridad de nuestra comunidad, necesitamos que subas tres imágenes.",
            textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium
        )

        // --- Tus tarjetas de subida de documentos ---
        DocumentUploadCard(document = dniFrontDocument, onClick = { dniFrontLauncher.launch("image/*") })
        DocumentUploadCard(document = dniBackDocument, onClick = { dniBackLauncher.launch("image/*") })
        DocumentUploadCard(document = selfieDocument, onClick = { selfieLauncher.launch("image/*") })

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                if (userId != -1 && allDocumentsSelected) {
                    viewModel.submitForVerification(
                        userId,
                        dniFrontDocument.uri!!,
                        dniBackDocument.uri!!,
                        selfieDocument.uri!!
                    ) { success ->
                        if (!success) {
                            Toast.makeText(context, "Error al enviar la solicitud", Toast.LENGTH_LONG).show()
                        }
                        // La pantalla cambiará a "En Revisión" automáticamente gracias al Flow
                    }
                }
            },
            enabled = allDocumentsSelected,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = if (allDocumentsSelected) "Enviar a Revisión" else "Faltan documentos",
                fontSize = 18.sp
            )
        }
    }
}

// --- 5. COMPONENTES DE UI REUTILIZABLES ---

// Tu tarjeta de subida, ligeramente adaptada para DocumentUploadStatus
@Composable
private fun DocumentUploadCard(document: DocumentToUpload, onClick: () -> Unit) {
    val statusColor = if (document.status == DocumentUploadStatus.SELECTED) Color(0xFF2196F3) else Color.Gray // Azul si está seleccionado, Gris si no
    val statusIcon = if (document.status == DocumentUploadStatus.SELECTED) Icons.Default.CheckCircle else Icons.Default.CloudUpload
    val statusText = if (document.status == DocumentUploadStatus.SELECTED) "ADJUNTADO" else "SUBIR FOTO"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).border(1.dp, statusColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = document.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = document.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(text = statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
        }
    }
}

// Mi vista de estado, sin cambios
@Composable
private fun StatusView(icon: ImageVector, title: String, message: String, iconColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(100.dp), tint = iconColor)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
