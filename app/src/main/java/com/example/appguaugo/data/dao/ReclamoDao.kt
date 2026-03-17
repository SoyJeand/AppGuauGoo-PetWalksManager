package com.example.appguaugo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.appguaugo.data.entity.ReclamoEntity

@Dao
interface ReclamoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReclamo(reclamo: ReclamoEntity)

    // Opcional: Podrías añadir métodos para obtener los reclamos de un usuario, etc.
    // @Query("SELECT * FROM reclamos WHERE userId = :userId ORDER BY fecha DESC")
    // fun getReclamosByUser(userId: Int): Flow<List<Reclamo>>
}
