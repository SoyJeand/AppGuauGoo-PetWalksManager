package com.example.appguaugo.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appguaugo.application.GuauApp
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.data.repository.ClienteRepository
import com.example.appguaugo.ui.theme.GuauYellowDark
import com.example.appguaugo.viewmodel.RequestWalkViewModel
import com.example.appguaugo.viewmodel.RequestWalkViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsperandoOfertasScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Obtener ID del cliente logueado
    val prefs = remember { context.getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE) }
    val loggedInClienteId = remember { prefs.getInt("logged_in_user_id", -1) }

    // 2. Instanciar Repositorio y ViewModel
    val repository = remember {
        ClienteRepository(
            GuauApp.db.clienteDao(),
            GuauApp.db.mascotaDao(),
            GuauApp.db.solicitudPaseoDao(),
            GuauApp.db.reclamoDao(),
            GuauApp.db.paseadorVerificacionDao()
        )
    }
    val viewModel: RequestWalkViewModel = viewModel(
        factory = RequestWalkViewModelFactory(repository)
    )

    // 3. Estados de la UI
    // Usamos collectAsState directamente desde el flujo para mayor reactividad
    var activeRequestId by remember { mutableStateOf(prefs.getInt("active_request_id", -1)) }

    // Si no hay ID guardado, intentamos buscar la última solicitud de este cliente
    LaunchedEffect(loggedInClienteId) {
        if (activeRequestId == -1 && loggedInClienteId != -1) {
            val ultimasSolicitudes = repository.getSolicitudesByCliente(loggedInClienteId).firstOrNull()
            // Tomamos la última solicitud que esté en estado BUSCANDO
            val ultimaActiva = ultimasSolicitudes?.firstOrNull { it.estado == "BUSCANDO" }
            if (ultimaActiva != null) {
                activeRequestId = ultimaActiva.id
                // Guardamos para futuras referencias
                prefs.edit().putInt("active_request_id", ultimaActiva.id).apply()
            }
        }
    }

    // Observamos la solicitud específica en tiempo real
    val activeRequestState by remember(activeRequestId) {
        if (activeRequestId != -1) {
            repository.getRequestById(activeRequestId)
        } else {
            // Flow vacío si no hay ID válido
            kotlinx.coroutines.flow.flowOf(null)
        }
    }.collectAsState(initial = null)


    // 4. Estructura de la Pantalla
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de tu Solicitud") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Forzar refresco (aunque el Flow debería ser automático)
                        val currentId = activeRequestId
                        activeRequestId = -1
                        scope.launch {
                            kotlinx.coroutines.delay(100)
                            activeRequestId = currentId
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        }
    ) { paddingValues ->

        // Lógica de visualización simplificada para depuración
        if (loggedInClienteId == -1) {
            EmptyState(paddingValues, "Error: No hay usuario logueado.")
        } else if (activeRequestId == -1) {
            EmptyState(paddingValues, "No se encontró ninguna solicitud activa para este usuario.")
        } else if (activeRequestState == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Cargando solicitud...", Modifier.padding(top = 48.dp))
            }
        } else {
            // ¡Aquí es donde mostramos los datos!
            val request = activeRequestState!!

            if (request.estado == "CONFIRMADO" || request.estado == "EN_PASEO") {
                // Si ya se confirmó, mostramos mensaje de éxito
                Column(
                    Modifier.fillMaxSize().padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("¡Paseo Confirmado!", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4CAF50))
                    Button(onClick = { navController.popBackStack() }) { Text("Volver al Inicio") }
                }
            } else {
                // Estado BUSCANDO con o sin ofertas
                OffersListContent(
                    paddingValues = paddingValues,
                    request = request, // Pasamos la entity viva
                    viewModel = viewModel,
                    repository = repository,
                    onAcceptSuccess = {
                        Toast.makeText(context, "¡Oferta aceptada!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun OffersListContent(
    paddingValues: PaddingValues,
    request: SolicitudPaseoEntity,
    viewModel: RequestWalkViewModel,
    repository: ClienteRepository,
    onAcceptSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Extraemos las ofertas del mapa. Si es nulo o vacío, la lista estará vacía.
    val offers = request.contraofertas.toList().sortedBy { it.second }

    // Mapa para guardar los nombres de los paseadores
    val paseadorNames = remember { mutableStateMapOf<Int, String>() }

    // Cargamos los nombres de los paseadores que hicieron ofertas
    LaunchedEffect(offers) {
        offers.forEach { (paseadorId, _) ->
            if (!paseadorNames.containsKey(paseadorId)) {
                scope.launch {
                    val paseador = repository.getClienteById(paseadorId).firstOrNull()
                    paseadorNames[paseadorId] = paseador?.nombres ?: "Paseador #$paseadorId"
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CABECERA DE LA SOLICITUD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = com.example.appguaugo.presentation.login.GuauYellowDark)
            ) {
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Detalles de tu Solicitud", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Mascota: ${request.mascotaNombre}")
                    Text("Destino: ${request.destino}")
                    Text("Tu Oferta Inicial: S/ ${request.costoOfrecido}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Estado: ${request.estado}",
                        color = if(request.estado == "BUSCANDO") Color.Gray else Color.Blue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text(
                text = if (offers.isEmpty()) "Esperando ofertas..." else "Contraofertas Recibidas (${offers.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (offers.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Buscando paseadores cercanos...", color = Color.Gray)
                    }
                }
            }
        } else {
            items(offers) { (paseadorId, offerPrice) ->
                OfferCard(
                    paseadorName = paseadorNames[paseadorId] ?: "Cargando nombre...",
                    offerPrice = offerPrice,
                    isLowest = offerPrice == offers.first().second, // Ya está ordenada
                    onAccept = {
                        scope.launch {
                            viewModel.acceptOffer(
                                requestId = request.id,
                                paseadorId = paseadorId,
                                precioFinal = offerPrice,
                                onResult = { success, message ->
                                    if (success) {
                                        onAcceptSuccess()
                                    } else {
                                        Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OfferCard(
    paseadorName: String,
    offerPrice: Double,
    isLowest: Boolean,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowest) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface // Verde claro si es la más baja
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = paseadorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isLowest) {
                    Text("¡Mejor precio!", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "S/ $offerPrice",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAccept,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}

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
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}
