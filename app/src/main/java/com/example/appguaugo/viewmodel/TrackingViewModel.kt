
package com.example.appguaugo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TrackingViewModel(
    private val repository: ClienteRepository,
    private val requestId: Int
) : ViewModel() {

    private val _requestState = MutableStateFlow<SolicitudPaseoEntity?>(null)
    val requestState: StateFlow<SolicitudPaseoEntity?> = _requestState.asStateFlow()

    private val _walkStatus = MutableStateFlow("Cargando...")
    val walkStatus: StateFlow<String> = _walkStatus.asStateFlow()

    init {
        loadWalkRequest()
    }

    private fun loadWalkRequest() {
        viewModelScope.launch {
            repository.getRequestById(requestId).collect { request ->
                _requestState.value = request
                // Lógica simple para actualizar el estado del paseo para la UI
                _walkStatus.value = when (request?.estado) {
                    "CONFIRMADO" -> "Paseador en camino"
                    "EN_PASEO" -> "Paseo en progreso"
                    "FINALIZADO" -> "Paseo finalizado"
                    else -> "Esperando inicio..."
                }
            }
        }
    }

    // --- ACCIONES DEL PASEADOR ---

    fun startWalk(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (_requestState.value?.estado == "CONFIRMADO") {
                val success = repository.updateRequestState(requestId, "EN_PASEO")
                if (success) {
                    onResult(true, "¡Paseo iniciado!")
                } else {
                    onResult(false, "Error al iniciar el paseo.")
                }
            } else {
                onResult(false, "El paseo no está en estado 'CONFIRMADO'.")
            }
        }
    }

    fun finishWalk(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (_requestState.value?.estado == "EN_PASEO") {
                val success = repository.updateRequestState(requestId, "FINALIZADO")
                if (success) {
                    onResult(true, "Paseo finalizado con éxito.")
                } else {
                    onResult(false, "Error al finalizar el paseo.")
                }
            } else {
                onResult(false, "El paseo no está en progreso.")
            }
        }
    }
}

class TrackingViewModelFactory(
    private val repository: ClienteRepository,
    private val requestId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackingViewModel(repository, requestId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}