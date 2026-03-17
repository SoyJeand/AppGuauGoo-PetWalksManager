package com.example.appguaugo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel que gestiona la lógica específica para el Paseador, incluyendo:
 * - Mostrar solicitudes disponibles.
 * - Enviar contraofertas.
 */
class PaseadorRequestViewModel(
    private val repository: ClienteRepository,
    val paseadorId: Int // Se requiere el ID del paseador logueado para identificar sus ofertas
) : ViewModel() {

    // 1. OBTENER SOLICITUDES EN ESTADO "BUSCANDO"
    // Usamos stateIn para exponer la lista de solicitudes disponibles a la UI en tiempo real
    val availableRequests: StateFlow<List<SolicitudPaseoEntity>> =
        repository.getAvailableRequests().stateIn(
            scope = viewModelScope,
            // Empieza a recolectar cuando hay observadores, y se detiene 5s después
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Función para que el paseador introduzca una contraoferta.
     * * Lógica clave:
     * 1. Obtiene la solicitud por ID.
     * 2. Actualiza el mapa 'contraofertas', usando el 'paseadorId' como clave.
     * 3. Guarda la entidad modificada.
     * * @param requestId ID de la solicitud a modificar.
     * @param newPrice Precio propuesto por el paseador.
     */
    fun submitCounterOffer(requestId: Int, newPrice: Double) {
        viewModelScope.launch {
            try {
                // 1. Obtener la solicitud actual de la BD.
                // Usamos firstOrNull() para obtener el valor del Flow una sola vez.
                val currentRequest = repository.getRequestById(requestId).firstOrNull()
                    ?: return@launch // Si no existe, salir

                // 2. Crear el nuevo mapa de contraofertas mutable
                val updatedOffers = currentRequest.contraofertas.toMutableMap()
                updatedOffers[paseadorId] = newPrice // Agrega o actualiza la oferta del paseador actual

                // 3. Crear una nueva entidad SÓLO con la contraoferta actualizada
                val updatedEntity = currentRequest.copy(
                    contraofertas = updatedOffers
                    // El estado sigue siendo "BUSCANDO"
                )

                // 4. Guardar la entidad actualizada en la BD
                repository.updateSolicitudPaseo(updatedEntity)

            } catch (e: Exception) {
                // Manejo de error (puedes añadir un evento de UI para mostrar un Toast)
                Log.e("PaseadorVM", "Error al enviar contraoferta: ${e.message}")
            }
        }
    }
}

// =========================================================================
// FACTORY para la inyección manual del ViewModel
// =========================================================================

class PaseadorRequestViewModelFactory(
    private val repository: ClienteRepository,
    private val paseadorId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaseadorRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaseadorRequestViewModel(repository, paseadorId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}