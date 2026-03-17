package com.example.appguaugo.viewmodel

import androidx.lifecycle.*
import com.example.appguaugo.data.entity.MascotaEntity
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.util.Log // Añadimos esto para el manejo de errores

class RequestWalkViewModel(private val repository: ClienteRepository) : ViewModel() {

    private val _pets = MutableStateFlow<List<MascotaEntity>>(emptyList())
    // Un StateFlow PÚBLICO e inmutable que la UI puede observar de forma segura.
    val pets: StateFlow<List<MascotaEntity>> = _pets.asStateFlow()

    fun loadPetsForOwner(ownerId: Int) {
        // Lanzamos una coroutine en el scope del ViewModel
        viewModelScope.launch {
            // Usamos .collect para escuchar los cambios del Flow que viene del repositorio
            repository.getMascotasByDuenoId(ownerId).collect { petListFromDb ->
                // Actualizamos el valor de nuestro StateFlow, lo que notificará a la UI
                _pets.value = petListFromDb
            }
        }
    }

    fun guardarSolicitud(
        clienteId: Int,
        origen: String,
        destino: String,
        mascotaNombre: String,
        tipoPaseo: String,
        observaciones: String,
        costoOfrecido: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val nuevaSolicitud = SolicitudPaseoEntity(
                    clienteId = clienteId,
                    origen = origen,
                    destino = destino,
                    mascotaNombre = mascotaNombre,
                    tipoPaseo = tipoPaseo,
                    observaciones = observaciones,
                    costoOfrecido = costoOfrecido,
                    estado = "BUSCANDO" // Aseguramos que el estado inicial es BUSCANDO
                )
                repository.insertSolicitudPaseo(nuevaSolicitud)
                onResult(true, "Solicitud guardada con éxito")
            } catch (e: Exception) {
                onResult(false, "Error al guardar: ${e.message}")
            }
        }
    }

    // =========================================================================
    // ▼▼▼ FUNCIÓN CLAVE PARA ACEPTAR LA OFERTA DE UN PASEADOR ▼▼▼
    // =========================================================================

    /**
     * Función que permite al cliente aceptar una contraoferta.
     * Cambia el estado a "CONFIRMADO" y asigna el paseador y precio final.
     * @param requestId ID de la solicitud a modificar.
     * @param paseadorId ID del paseador cuya oferta fue aceptada.
     * @param precioFinal Precio aceptado.
     */
    fun acceptOffer(requestId: Int, paseadorId: Int, precioFinal: Double, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // 1. Obtener la solicitud actual (usando firstOrNull() para obtener el valor del Flow)
            val currentRequest = repository.getRequestById(requestId).firstOrNull()
                ?: run {
                    onResult(false, "Error: Solicitud no encontrada.")
                    return@launch
                }

            // 2. Crear la entidad actualizada con los nuevos valores:
            val updatedEntity = currentRequest.copy(
                estado = "CONFIRMADO", // Cambiar el estado a CONFIRMADO
                paseadorIdAceptado = paseadorId, // Asignar el paseador
                precioFinal = precioFinal // Guardar el precio final
                // Contraofertas y otros campos quedan como estaban.
            )

            // 3. Guardar la entidad actualizada en la BD
            try {
                repository.updateSolicitudPaseo(updatedEntity)
                onResult(true, "¡Paseo confirmado con éxito! El paseador ha sido notificado.")
            } catch (e: Exception) {
                Log.e("RequestWalkVM", "Error al confirmar el paseo: ${e.message}")
                onResult(false, "Error al guardar la confirmación.")
            }
        }
    }
}

class RequestWalkViewModelFactory(private val repository: ClienteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RequestWalkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RequestWalkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}