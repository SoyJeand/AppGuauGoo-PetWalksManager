package com.example.appguaugo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appguaugo.data.entity.MascotaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMascota(mascota: MascotaEntity)

    // Obtiene un Flow con la lista de mascotas de un dueño específico.
    // El Flow se actualizará automáticamente si se añade o borra una mascota.
    @Query("SELECT * FROM mascota WHERE duenoId = :duenoId ORDER BY nombre ASC")
    fun getMascotasByDuenoId(duenoId: Int): Flow<List<MascotaEntity>>

}