package com.example.appguaugo.data.repository

import com.example.appguaugo.data.dao.ClienteDao
import com.example.appguaugo.data.dao.MascotaDao
import com.example.appguaugo.data.dao.PaseadorVerificacionDao
import com.example.appguaugo.data.dao.ReclamoDao
import com.example.appguaugo.data.dao.SolicitudPaseoDao
import kotlinx.coroutines.flow.firstOrNull
import com.example.appguaugo.data.entity.ClienteEntity
import com.example.appguaugo.data.entity.MascotaEntity
import com.example.appguaugo.data.entity.PaseadorVerificacionEntity
import com.example.appguaugo.data.entity.ReclamoEntity
import com.example.appguaugo.data.entity.SolicitudPaseoEntity
import com.example.appguaugo.data.entity.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClienteRepository(
    private val clienteDao: ClienteDao,
    private val mascotaDao: MascotaDao,
    private val solicitudPaseoDao: SolicitudPaseoDao,
    private val reclamoDao: ReclamoDao,
    private val paseadorVerificacionDao: PaseadorVerificacionDao
) {

    // Llama a la función correspondiente en el DAO para insertar un cliente.
    suspend fun insertCliente(cliente: ClienteEntity): Long {
        return clienteDao.insertCliente(cliente)
    }

    // Llama a la función correspondiente en el DAO para validar un cliente.
    suspend fun validarCliente(correo: String, contrasenha: String): ClienteEntity? {
        return clienteDao.validarCliente(correo, contrasenha)
    }

    fun getClienteById(userId: Int): Flow<ClienteEntity?> {
        return clienteDao.getClienteById(userId)
    }

    // FUNCIONES DE LAS MASCOTAS
    fun getMascotasByDuenoId(duenoId: Int): Flow<List<MascotaEntity>> {
        return mascotaDao.getMascotasByDuenoId(duenoId)
    }

    suspend fun insertMascota(mascota: MascotaEntity) {
        mascotaDao.insertMascota(mascota)
    }

    // FUNCIONES DE LA SOLICITUD DE PASEO CLI
    suspend fun insertSolicitudPaseo(solicitud: SolicitudPaseoEntity) {
        solicitudPaseoDao.insertSolicitud(solicitud)
    }

    // =========================================================================
    // ▼▼▼ FUNCIONES AÑADIDAS PARA CONTRAOFERTA/ACEPTACIÓN ▼▼▼
    // =========================================================================

    /**
     * Obtiene una solicitud por su ID. Necesaria para modificar la entidad en el ViewModel.
     * @param requestId ID de la solicitud.
     */

    fun getSolicitudesByCliente(clienteId: Int): Flow<List<SolicitudPaseoEntity>> {
        // Asegúrate de que esta función exista en tu SolicitudPaseoDao
        return solicitudPaseoDao.getSolicitudesByCliente(clienteId)
    }

    // ▼▼▼ NUEVA: Lógica completa para aceptar una oferta/contraoferta ▼▼▼
    suspend fun aceptarPaseo(solicitudId: Int, paseadorId: Int, precioFinal: Double) {
        // 1. Obtenemos la solicitud actual
        val solicitudActual = solicitudPaseoDao.getRequestById(solicitudId).firstOrNull()

        if (solicitudActual != null) {
            // 2. Creamos la copia con los datos actualizados
            val solicitudAceptada = solicitudActual.copy(
                estado = "CONFIRMADO",          // Cambiamos estado
                paseadorIdAceptado = paseadorId, // Guardamos quién ganó
                precioFinal = precioFinal,       // Guardamos el precio final acordado
                // Opcional: Podrías limpiar las otras contraofertas si quieres ahorrar espacio,
                // pero dejarlas sirve de historial.
            )
            // 3. Guardamos en BD
            solicitudPaseoDao.updateSolicitud(solicitudAceptada)
        }
    }

    fun getRequestById(requestId: Int): Flow<SolicitudPaseoEntity?> {
        // Delega la llamada al DAO
        return solicitudPaseoDao.getRequestById(requestId)
    }

    /**
     * Actualiza una solicitud existente. Usada para guardar contraofertas o aceptar un paseo.
     * @param solicitud La entidad con los campos modificados (e.g., contraofertas, estado).
     */
    suspend fun updateSolicitudPaseo(solicitud: SolicitudPaseoEntity) {
        // Delega la llamada al DAO (usando la función que definimos en SolicitudPaseoDao)
        solicitudPaseoDao.updateSolicitud(solicitud)
    }

    /**
     * Actualiza solo el campo de estado de una solicitud de paseo.
     * @param requestId El ID de la solicitud a actualizar.
     * @param newState El nuevo estado ("CONFIRMADO", "EN_PASEO", "FINALIZADO", etc.).
     * @return true si la actualización fue exitosa.
     */
    suspend fun updateRequestState(requestId: Int, newState: String): Boolean {
        // 1. Obtenemos la solicitud actual. Usamos firstOrNull para tomar el valor del Flow.
        val request = solicitudPaseoDao.getRequestById(requestId).firstOrNull()

        return if (request != null) {
            // 2. Creamos una copia de la entidad con el nuevo estado
            val updatedRequest = request.copy(estado = newState)

            // 3. Usamos la función @Update de tu DAO
            solicitudPaseoDao.updateSolicitud(updatedRequest)
            true
        } else {
            false
        }
    }

    /**
     * Obtiene todas las solicitudes en estado 'BUSCANDO'. (Usado por el Paseador).
     */
    fun getAvailableRequests(): Flow<List<SolicitudPaseoEntity>> {
        return solicitudPaseoDao.getAvailableRequests()
    }


    // =========================================================================
    // ▲▲▲ FIN DE FUNCIONES AÑADIDAS ▲▲▲
    // =========================================================================


    // FUNCIONES PARA RECLAMOS
    suspend fun guardarReclamo(reclamo: ReclamoEntity) {
        reclamoDao.insertReclamo(reclamo)
    }

    //FUNCIONES PARA VERIFICACION PASEADOR

    fun getEstadoVerificacion(paseadorId: Int): Flow<String> {
        // Devuelve "No verificado" por defecto si no hay ninguna entrada para ese usuario.
        return paseadorVerificacionDao.getEstadoVerificacion(paseadorId)
            .map { it ?: VerificationStatus.NO_VERIFICADO }
    }

    suspend fun guardarVerificacion(verificacion: PaseadorVerificacionEntity) {
        paseadorVerificacionDao.guardarOActualizarVerificacion(verificacion)
    }

    fun getVerificacion(paseadorId: Int): Flow<PaseadorVerificacionEntity?> {
        return paseadorVerificacionDao.getVerificacion(paseadorId)
    }
}