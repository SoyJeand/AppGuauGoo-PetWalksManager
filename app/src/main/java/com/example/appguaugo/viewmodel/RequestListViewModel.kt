package com.example.appguaugo.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestListViewModel(
    private val repository: ClienteRepository,
    private val clienteId: Int
) : ViewModel() {

    private val _requests = MutableStateFlow<List<SolicitudPaseoEntity>>(emptyList())
    val requests: StateFlow<List<SolicitudPaseoEntity>> = _requests.asStateFlow()

    init {
        loadRequests()
    }

    private fun loadRequests() {
        viewModelScope.launch {
            // Asegúrate de tener este método en tu repositorio
            repository.getSolicitudesByCliente(clienteId).collect { lista ->
                _requests.value = lista
            }
        }
    }

    /**
     * Acepta la contraoferta de un paseador específico.
     */
    fun aceptarContraoferta(solicitudId: Int, paseadorId: Int, montoAcordado: Double) {
        viewModelScope.launch {
            // 1. Actualizamos la solicitud:
            // - Estado pasa a CONFIRMADO
            // - Guardamos quién es el paseador aceptado
            // - Fijamos el precio final
            repository.aceptarPaseo(solicitudId, paseadorId, montoAcordado)
        }
    }
}

// ... (El Factory es igual al anterior)
