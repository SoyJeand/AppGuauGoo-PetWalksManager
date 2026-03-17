package com.example.appguaugo.presentation.home

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appguaugo.application.GuauApp
import com.example.appguaugo.application.UserRole
import com.example.appguaugo.application.UserSessionManager
import com.example.appguaugo.data.repository.ClienteRepository
import com.example.appguaugo.presentation.walker_home.WalkerHomeScreen // Importación de la pantalla del paseador
import com.example.appguaugo.viewmodel.ProfileUiState
import com.example.appguaugo.viewmodel.ProfileViewModel
import com.example.appguaugo.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

// Importación necesaria para RequestWalkScreen (asumiendo que está en el mismo paquete)
// import com.example.appguaugo.presentation.home.RequestWalkScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    navController: NavController
) {
    // --- 1. Observar el rol actual del usuario y obtener Repositorio ---
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE) }
    val loggedInUserId = remember { prefs.getInt("logged_in_user_id", -1) }

    val repository = remember {
        ClienteRepository(
            GuauApp.db.clienteDao(),
            GuauApp.db.mascotaDao(),
            GuauApp.db.solicitudPaseoDao(),
            GuauApp.db.reclamoDao(),
            GuauApp.db.paseadorVerificacionDao()
        )
    }

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(repository, loggedInUserId)
    )

    // --- 2. ESTADOS OBSERVABLES ---
    val profileState by profileViewModel.uiState.collectAsState()
    val userName = when (val state = profileState) {
        is ProfileUiState.Success -> state.user.nombres
        is ProfileUiState.Loading -> "Cargando..."
        is ProfileUiState.Error -> "Error"
    }

    val currentRole by UserSessionManager.currentRole.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- 3. DEFINICIÓN DE EVENTOS DE NAVEGACIÓN (Para el Drawer) ---
    val onProfileClick: () -> Unit = {
        if (loggedInUserId != -1) {
            navController.navigate("profile/$loggedInUserId")
        } else {
            Toast.makeText(context, "Error: No se pudo encontrar el ID de usuario.", Toast.LENGTH_SHORT).show()
        }
        scope.launch { drawerState.close() }
    }

    val onMyPetsClick: () -> Unit = {
        navController.navigate("my_pets")
        scope.launch { drawerState.close() }
    }

    val onClaimsClick: () -> Unit = {
        navController.navigate("claims")
        scope.launch { drawerState.close() }
    }

    val onPaseoClick: () -> Unit = {
        navController.navigate("esperando_ofertas")
        scope.launch { drawerState.close() }
    }


    val onHistorialPaseosClick: () -> Unit = {
        navController.navigate("walk_history")
        scope.launch { drawerState.close() }
    }

    val onVerificationClick: () -> Unit = {
        navController.navigate("verification_screen")
        scope.launch { drawerState.close() }
    }


    val onLogoutClick: () -> Unit = {
        prefs.edit().clear().apply()
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                userName =  userName,
                onProfileClick = onProfileClick,
                onPaseoClick = onPaseoClick,
                onHistorialPaseosClick = onHistorialPaseosClick,
                onMyPetsClick = onMyPetsClick,
                onClaimsClick = onClaimsClick,
                // --- Estado de Verificación para el Paseador ---
                verificationStatus = when (val state = profileState) {
                    is ProfileUiState.Success -> state.verificationStatus
                    else -> ""
                },
                onVerificationClick = onVerificationClick, // Acción de navegación a la nueva pantalla
                // --- Fin de Verificación ---
                onLogoutClick = onLogoutClick,

                currentRole = currentRole,
                onSwitchRole = { UserSessionManager.switchRole() }
            )
        }
    ) {
        // --- 4. Lógica condicional para mostrar la pantalla principal correcta ---
        when (currentRole) {
            UserRole.CLIENTE -> {
                RequestWalkScreen(
                    navController = navController, // Necesario si RequestWalkScreen tiene navegación interna
                    openDrawer = { scope.launch { drawerState.open() } }
                )
            }
            UserRole.PASEADOR -> {
                // Muestra la pantalla principal del Paseador (que internamente maneja la verificación)
                WalkerHomeScreen(
                    navController = navController, // Pasamos el NavController por si necesita navegar a verificación
                    openDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}