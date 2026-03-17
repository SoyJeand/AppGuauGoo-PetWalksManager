package com.example.appguaugo.presentation.tracking

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appguaugo.application.GuauApp
import com.example.appguaugo.data.repository.ClienteRepository
import com.example.appguaugo.viewmodel.TrackingViewModel
import com.example.appguaugo.viewmodel.TrackingViewModelFactory
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------------
// NOTA: La pantalla ahora recibe el NavController y el ID de la solicitud
// ---------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    navController: NavController,
    requestId: Int,
    onWalkFinished: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE) }
    val loggedInUserId = remember { prefs.getInt("logged_in_user_id", -1) }

    val repository = remember {
        ClienteRepository(
            GuauApp.db.clienteDao(), GuauApp.db.mascotaDao(), GuauApp.db.solicitudPaseoDao(),
            GuauApp.db.reclamoDao(), GuauApp.db.paseadorVerificacionDao()
        )
    }

    val viewModel: TrackingViewModel = viewModel(
        factory = TrackingViewModelFactory(repository, requestId)
    )

    val request by viewModel.requestState.collectAsState()
    val walkStatus by viewModel.walkStatus.collectAsState()
    val scope = rememberCoroutineScope()

    // Determinamos el rol del usuario actual en esta solicitud
    val isWalker = loggedInUserId == request?.paseadorIdAceptado
    val isClient = loggedInUserId == request?.clienteId

    // 1. Si la solicitud no existe o está finalizada, navegamos de vuelta al home.
    LaunchedEffect(request?.estado) {
        if (request?.estado == "FINALIZADO") {
            // El cliente debe ser redirigido a la pantalla de calificación
            if (isClient) {
                onWalkFinished() // Esto navega a la pantalla de Rating
            } else {
                // El paseador solo vuelve al home
                navController.popBackStack()
                Toast.makeText(context, "Paseo finalizado", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Siguiendo el Paseo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Pets, contentDescription = "Paseo")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- Placeholder para el Mapa ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Google Maps API:\nMostrar ruta Cliente ↔ Paseador",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.DarkGray
                )
            }

            // --- Tarjeta de Estado y Acciones ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Estado: $walkStatus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Paseador: ${request?.paseadorIdAceptado ?: "Cargando..."}")
                    Text("Precio Final: S/${request?.precioFinal ?: "0.00"}")

                    if (isWalker && request?.estado == "CONFIRMADO") {
                        // Botón de INICIO (Solo visible para el paseador cuando está confirmado)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.startWalk { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar Paseo")
                        }
                    } else if (isWalker && request?.estado == "EN_PASEO") {
                        // Botón de FINALIZAR (Solo visible para el paseador cuando está en progreso)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.finishWalk { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Finalizar Paseo")
                        }
                    } else {
                        // Cliente o estado de espera
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isClient) "Esperando acción del paseador..." else "Paseador no asignado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}