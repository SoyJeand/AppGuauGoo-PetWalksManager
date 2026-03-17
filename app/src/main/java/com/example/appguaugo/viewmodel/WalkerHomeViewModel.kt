package com.example.appguaugo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appguaugo.data.entity.PaseadorVerificacionEntity
import com.example.appguaugo.data.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow

class WalkerHomeViewModel(private val repository: ClienteRepository) : ViewModel() {

    // Expone la función del repositorio directamente para que la UI la observe.
    fun getVerificationState(userId: Int): Flow<PaseadorVerificacionEntity?> {
        return repository.getVerificacion(userId)
    }
}

// Factory para inyectar el repositorio.
class WalkerHomeViewModelFactory(
    private val repository: ClienteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalkerHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalkerHomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

