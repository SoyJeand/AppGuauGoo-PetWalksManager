package com.example.appguaugo.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appguaugo.data.entity.ReclamoEntity
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

// Estado para la UI
enum class ClaimUiState {
    Idle,      // Estado inicial
    Loading,   // Enviando
    Success,   // Éxito
    Error      // Error
}

class ClaimsViewModel(
    private val repository: ClienteRepository,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClaimUiState>(ClaimUiState.Idle)
    val uiState: StateFlow<ClaimUiState> = _uiState

    fun enviarReclamo(mensaje: String) {
        if (mensaje.length < 20) {
            // Podríamos exponer un evento de error de validación aquí si quisiéramos
            return
        }

        viewModelScope.launch {
            _uiState.value = ClaimUiState.Loading
            try {
                val nuevoReclamo = ReclamoEntity(
                    userId = userId,
                    mensaje = mensaje,
                    fecha = Date() // Fecha y hora actual
                )
                repository.guardarReclamo(nuevoReclamo)
                _uiState.value = ClaimUiState.Success
            } catch (e: Exception) {
                // Aquí podrías loguear el error `e`
                _uiState.value = ClaimUiState.Error
            }
        }
    }

    // Función para resetear el estado después de que la UI reacciona
    fun resetState() {
        _uiState.value = ClaimUiState.Idle
    }
}

// Factory para poder pasar parámetros (repository y userId) al ViewModel
class ClaimsViewModelFactory(
    private val repository: ClienteRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClaimsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClaimsViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

