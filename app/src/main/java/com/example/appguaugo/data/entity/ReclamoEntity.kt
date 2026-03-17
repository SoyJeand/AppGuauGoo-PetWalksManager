package com.example.appguaugo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reclamos")
data class ReclamoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Int, // El ID del usuario que hace el reclamo
    val mensaje: String,
    val fecha: Date,
    val estado: String = "Pendiente" // Puede ser "Pendiente", "En revisión", "Resuelto"
)
