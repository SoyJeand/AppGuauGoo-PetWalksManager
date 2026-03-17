package com.example.appguaugo.presentation.home

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appguaugo.application.GuauApp
import com.example.appguaugo.data.repository.ClienteRepository
import com.example.appguaugo.ui.theme.GuauYellow
import com.example.appguaugo.ui.theme.GuauYellowDark
import com.example.appguaugo.viewmodel.ClaimUiState
import com.example.appguaugo.viewmodel.ClaimsViewModel
import com.example.appguaugo.viewmodel.ClaimsViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimsScreen(
    onNavigateBack: () -> Unit
) {
    // 1. Estados para la UI
    val context = LocalContext.current

    // --- 1. Obtener dependencias y crear el ViewModel ---
    val prefs = context.getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
    val loggedInUserId = remember { prefs.getInt("logged_in_user_id", -1) }

    val repository = remember {
        ClienteRepository(
            GuauApp.db.clienteDao(), GuauApp.db.mascotaDao(),
            GuauApp.db.solicitudPaseoDao(), GuauApp.db.reclamoDao(),
            GuauApp.db.paseadorVerificacionDao()
        )
    }

    val viewModel: ClaimsViewModel = viewModel(
        factory = ClaimsViewModelFactory(repository, loggedInUserId)
    )

    // --- 2. Observar el estado del ViewModel ---
    val uiState by viewModel.uiState.collectAsState()
    val isSending = uiState == ClaimUiState.Loading

    var claimMessage by remember { mutableStateOf("") }

    // --- 3. Efecto para reaccionar a los cambios de estado (éxito, error) ---
    LaunchedEffect(uiState) {
        when (uiState) {
            ClaimUiState.Success -> {
                Toast.makeText(context, "Reclamo enviado con éxito.", Toast.LENGTH_LONG).show()
                viewModel.resetState() // Resetea el estado
                onNavigateBack()       // Navega hacia atrás
            }
            ClaimUiState.Error -> {
                Toast.makeText(context, "Error al enviar el reclamo. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {} // Idle o Loading, no hacemos nada aquí
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reclamos y Soporte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GuauYellow,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Sección de Información ---
            Text(
                text = "Contacta con Soporte",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Por favor, describe tu situación en detalle. Nuestro equipo revisará tu caso y se pondrá en contacto contigo a la brevedad.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Campo de Texto para el Reclamo ---
            OutlinedTextField(
                value = claimMessage,
                onValueChange = { claimMessage = it },
                label = { Text("Describe tu reclamo aquí...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                enabled = !isSending // Se deshabilita mientras se envía
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Botón de Envío ---
            Button(
                onClick = {
                    // --- 4. El OnClick ahora llama al ViewModel ---
                    if (claimMessage.length < 20) {
                        Toast.makeText(context, "Por favor, detalla más tu reclamo (mínimo 20 caracteres).", Toast.LENGTH_LONG).show()
                    } else {
                        viewModel.enviarReclamo(claimMessage)
                    }
                },
                enabled = !isSending, // Se deshabilita el botón mientras se envía
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GuauYellowDark)
            ) {
                if (isSending) {
                    // Muestra un indicador de carga mientras se envía
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    // Muestra el texto del botón
                    Text("Enviar Reclamo")
                }
            }
        }
    }
}
