package com.example.appguaugo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appguaugo.data.entity.ClienteEntity
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estados posibles de la UI del Perfil
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: ClienteEntity,
        val verificationStatus: String
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val repository: ClienteRepository,
    private val userId: Int // Recibe el ID del usuario a mostrar
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState
    init {
        if (userId != -1) {
            viewModelScope.launch {
                val userFlow = repository.getClienteById(userId) // Asumo que getClienteById es correcto
                val verificationStatusFlow = repository.getEstadoVerificacion(userId)

                userFlow.combine(verificationStatusFlow) { user, status ->
                    if (user != null) {
                        ProfileUiState.Success(user, status)
                    } else {
                        // --- ▼▼▼ CORRECCIÓN 1 ▼▼▼ ---
                        // Llama al constructor con un mensaje
                        ProfileUiState.Error("Usuario no encontrado.")
                    }
                }.catch { e -> // Opcional: captura la excepción para un mensaje más útil
                    // --- ▼▼▼ CORRECCIÓN 2 ▼▼▼ ---
                    // Llama al constructor con el mensaje del error
                    emit(ProfileUiState.Error("Error al cargar los datos: ${e.message}"))
                }.collect { state ->
                    _uiState.value = state
                }
            }
        } else {
            // --- ▼▼▼ CORRECCIÓN 3 ▼▼▼ ---
            // Llama al constructor con un mensaje
            _uiState.value = ProfileUiState.Error("ID de usuario no válido.")
        }
    }
}

