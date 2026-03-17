package com.example.appguaugo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import kotlinx.coroutines.flow.Flow // Necesario para obtener datos en tiempo real

@Dao
interface SolicitudPaseoDao {

    // 1. Insertar una nueva solicitud
    // Usamos REPLACE por si el cliente intenta mandar dos solicitudes casi al mismo tiempo,
    // o por si queremos actualizar la solicitud con un nuevo ID (aunque lo ideal es usar @Update)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: SolicitudPaseoEntity): Long
    // NOTA: Si esta función devuelve Long, devolverá el ID de la fila insertada.

    // =========================================================================
    // ▼▼▼ FUNCIONES AÑADIDAS PARA LA LÓGICA DE CONTRAOFERTA/ACEPTACIÓN ▼▼▼
    // =========================================================================

    /**
     * 2. Obtener solicitudes activas en estado 'BUSCANDO'. (Usado por el Paseador).
     * Retorna un Flow para que la UI se actualice automáticamente cuando llegue una nueva oferta.
     */
    @Query("SELECT * FROM solicitud_paseo WHERE estado = 'BUSCANDO'")
    fun getAvailableRequests(): Flow<List<SolicitudPaseoEntity>>

    // Esta es necesaria para la lista del cliente
    @Query("SELECT * FROM solicitud_paseo WHERE clienteId = :clienteId ORDER BY id DESC")
    fun getSolicitudesByCliente(clienteId: Int): Flow<List<SolicitudPaseoEntity>>

    // Esta es necesaria para getAvailableRequests

    /**
     * 3. Obtener una solicitud por su ID. Necesaria para cargar y modificarla en el ViewModel.
     * Retorna un Flow para observar cambios (e.g., cuando llega una contraoferta).
     */
    @Query("SELECT * FROM solicitud_paseo WHERE id = :requestId")
    fun getRequestById(requestId: Int): Flow<SolicitudPaseoEntity?>

    /**
     * 4. Actualiza una solicitud existente. Usada para guardar:
     * - Las contraofertas del paseador.
     * - La aceptación del cliente (cambiando estado, precioFinal y paseadorIdAceptado).
     */
    @Update
    suspend fun updateSolicitud(solicitud: SolicitudPaseoEntity)
}