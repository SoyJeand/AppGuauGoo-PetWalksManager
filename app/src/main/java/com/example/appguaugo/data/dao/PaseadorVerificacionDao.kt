package com.example.appguaugo.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.appguaugo.data.entity.PaseadorVerificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaseadorVerificacionDao {

    // Upsert = Update or Insert. Perfecto para este caso.
    @Upsert
    suspend fun guardarOActualizarVerificacion(verificacion: PaseadorVerificacionEntity)

    // Usamos Flow para que la UI se actualice automáticamente si el estado cambia
    @Query("SELECT estado FROM paseador_verificacion WHERE paseadorId = :paseadorId")
    fun getEstadoVerificacion(paseadorId: Int): Flow<String?>

    // Opcional: para obtener el objeto completo
    @Query("SELECT * FROM paseador_verificacion WHERE paseadorId = :paseadorId")
    fun getVerificacion(paseadorId: Int): Flow<PaseadorVerificacionEntity?>
}
