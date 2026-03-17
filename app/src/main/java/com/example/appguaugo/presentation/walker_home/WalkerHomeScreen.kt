package com.example.appguaugo.presentation.walker_home

import android.content.Context
import android.widget.Toast


import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions



import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // <<<<<<<<<<<<< IMPORTACIÓN NECESARIA
import com.example.appguaugo.R

import com.example.appguaugo.application.GuauApp
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.data.entity.VerificationStatus
import com.example.appguaugo.data.repository.ClienteRepository
import com.example.appguaugo.ui.theme.GuauYellowDark
import com.example.appguaugo.viewmodel.PaseadorRequestViewModel
import com.example.appguaugo.viewmodel.PaseadorRequestViewModelFactory
import com.example.appguaugo.viewmodel.WalkerHomeViewModel
import com.example.appguaugo.viewmodel.WalkerHomeViewModelFactory
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkerHomeScreen(
    navController: NavController, // <<<<<<<<<<<<< AÑADIDO: NavController
    openDrawer: () -> Unit
) {
    // --- 1. INICIALIZACIÓN DE DATOS Y VIEWMODEL ---
    val context = LocalContext.current
    val loggedInUserId = remember {
        context.getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)
    }

    if (loggedInUserId == -1) {
        Text("Error: Usuario no logueado", modifier = Modifier.padding(16.dp))
        return
    }

    val repository = remember {
        ClienteRepository(
            GuauApp.db.clienteDao(), GuauApp.db.mascotaDao(), GuauApp.db.solicitudPaseoDao(),
            GuauApp.db.reclamoDao(), GuauApp.db.paseadorVerificacionDao()
        )
    }

    val viewModel: WalkerHomeViewModel = viewModel(
        factory = WalkerHomeViewModelFactory(repository)
    )

    val verificationState by viewModel.getVerificationState(loggedInUserId).collectAsState(initial = null)

    // --- 2. SCAFFOLD PRINCIPAL ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo Paseador") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- 3. LÓGICA DE VISUALIZACIÓN ---
        Box(modifier = Modifier.padding(paddingValues)) {
            Crossfade(targetState = verificationState?.estado, label = "verification_crossfade") { status ->
                when (status) {
                    VerificationStatus.VERIFICADO -> {
                        LookingForWalksScreen(loggedInUserId = loggedInUserId, repository = repository)
                    }
                    VerificationStatus.PENDIENTE -> {
                        PendingVerificationScreen()
                    }
                    else -> {
                        VerificationRequiredScreen(navController = navController) // Pasamos navController aquí
                    }
                }
            }
        }
    }
}

// También debes actualizar la función VerificationRequiredScreen si usa navController para ir a verificación
@Composable
fun VerificationRequiredScreen(navController: NavController) {
    // ... (Mantener el contenido anterior y usar navController.navigate("verification_screen")
    // en un botón si lo tienes)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = "Verificación Requerida",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Verificación Requerida",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Para poder recibir solicitudes de paseo, primero debes completar la verificación de tu identidad. El proceso es breve y fácil.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("verification_screen") }) {
            Text("Ir a Verificación de Datos")
        }
    }
}

// =========================================================================
// CAMBIO CLAVE: Esta pantalla ahora muestra la lista de solicitudes.
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookingForWalksScreen(loggedInUserId: Int, repository: ClienteRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Inyección del ViewModel del Paseador
    val viewModel: PaseadorRequestViewModel = viewModel(
        factory = PaseadorRequestViewModelFactory(repository, loggedInUserId)
    )

    // Observar las solicitudes disponibles
    val availableRequests by viewModel.availableRequests.collectAsState()

    // Estado para el BottomSheet de la contraoferta
    var showOfferSheet by remember { mutableStateOf(false) }
    var offerValue by remember { mutableStateOf("") }
    var selectedRequestToOffer by remember { mutableStateOf<SolicitudPaseoEntity?>(null) }
    val costSheetState = rememberModalBottomSheetState()


    if (availableRequests.isEmpty()) {
        EmptyState(
            paddingValues = PaddingValues(0.dp), // Padding ya se aplica en el WalkerHomeScreen
            message = "No hay solicitudes de paseo activas en tu zona."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            item {
                Text(
                    text = "Nuevas Solicitudes de Paseo (Buscando Paseador)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(availableRequests) { request ->
                RequestCard(
                    request = request,
                    paseadorId = loggedInUserId,
                    onOfferClick = {
                        selectedRequestToOffer = request
                        showOfferSheet = true
                        offerValue = "" // Limpiar el valor anterior
                    }
                )
            }
        }
    }

    // BottomSheet para la Contraoferta
    if (showOfferSheet && selectedRequestToOffer != null) {
        NumberSheet(
            sheetState = costSheetState,
            value = offerValue,
            onValueChange = { offerValue = it },
            title = "Tu contraoferta S/.",
            onDismiss = {
                showOfferSheet = false
                selectedRequestToOffer = null
            },
            onConfirm = {
                val offerPrice = offerValue.toDoubleOrNull()
                if (offerPrice == null || offerPrice <= 0) {
                    Toast.makeText(context, "Ingresa un precio válido.", Toast.LENGTH_SHORT).show()
                    return@NumberSheet
                }

                showOfferSheet = false
                // Llamar a la función del ViewModel para enviar la oferta
                viewModel.submitCounterOffer(
                    requestId = selectedRequestToOffer!!.id,
                    newPrice = offerPrice
                )

                Toast.makeText(context, "Oferta enviada por S/$offerPrice", Toast.LENGTH_SHORT).show()
                selectedRequestToOffer = null
            }
        )
    }
}


// =========================================================================
// PANTALLAS DE ESTADO DE VERIFICACIÓN (SIN CAMBIOS)
// =========================================================================

@Composable
fun PendingVerificationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.HourglassTop, // Un ícono que representa espera/tiempo
            contentDescription = "Verificación Pendiente",
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFE65100) // Un color naranja para indicar "en proceso"
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Solicitud en Revisión",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¡Gracias! Hemos recibido tus documentos y los estamos revisando. Este proceso suele tardar entre 24 y 48 horas.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Te notificaremos tan pronto como tu cuenta sea aprobada. ¡Agradecemos tu paciencia!",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}


// =========================================================================
// COMPONENTES DE UI REQUERIDOS (Tomados de la propuesta de RequestsScreen)
// =========================================================================

// --- COMPONENTE: TARJETA DE SOLICITUD ---
@Composable
fun RequestCard(
    request: SolicitudPaseoEntity,
    paseadorId: Int,
    onOfferClick: () -> Unit
) {
    val currentOffer = request.contraofertas[paseadorId]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Desde: ${request.origen}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.Pets, contentDescription = "Mascota", tint = GuauYellowDark)
            }

            Spacer(Modifier.height(4.dp))
            Text(text = "Hasta: ${request.destino}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Tipo: ${request.tipoPaseo} (${request.mascotaNombre})", style = MaterialTheme.typography.bodyMedium)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Precio Cliente: S/${request.costoOfrecido}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Mostrar la oferta actual si existe
            if (currentOffer != null) {
                Text(
                    text = "Tu Oferta: S/${String.format("%.2f", currentOffer)} (Enviada)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onOfferClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GuauYellowDark)
            ) {
                Text(if (currentOffer != null) "Re-ofertar" else "Enviar Oferta")
            }
        }
    }
}


// --- COMPONENTE: ESTADO VACÍO ---
@Composable
fun EmptyState(paddingValues: PaddingValues, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = "Buscando",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

// Archivo: WalkerHomeScreen.kt

// --- COMPONENTE: NumberSheet (Reutilizado y Adaptado) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberSheet(
    sheetState: SheetState,
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(title, fontSize = 20.sp)

            Spacer(Modifier.height(16.dp))

            TextField(
                value = value,
                // Solo acepta dígitos y un punto decimal
                onValueChange = { onValueChange(it.filter { char -> char.isDigit() || char == '.' }) },
                // =========================================================================
                // >>>>>> CORRECCIÓN AQUÍ: USAMOS NOMBRES CORTOS RESOLVIENDO LAS IMPORTACIONES
                // =========================================================================
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, // CORREGIDO
                    imeAction = ImeAction.Done // CORREGIDO
                ),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }), // CORREGIDO
                // =========================================================================
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar Oferta")
            }
        }
    }
}