package com.example.appguaugo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import java.util.Date


@Entity(
    tableName = "solicitud_paseo",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id_cliente"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clienteId"])]
)
data class SolicitudPaseoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clienteId: Int,
    val origen: String,
    val destino: String,
    val mascotaNombre: String, // Guardamos el nombre para mostrarlo fácilmente
    val tipoPaseo: String,
    val observaciones: String,
    val costoOfrecido: Double,
    val fechaSolicitud: Date = Date(), // Guarda la fecha y hora de la solicitud
    val estado: String = "BUSCANDO", // Estados: BUSCANDO, CONFIRMADO, CANCELADO, FINALIZADO

    // --- CAMPOS NUEVOS PARA LA FUNCIONALIDAD ---

    // Almacena las ofertas: Clave(Paseador ID) -> Valor(Precio Ofrecido).
    // Manejado por el MapConverter que añadimos a Converters.kt
    val contraofertas: Map<Int, Double> = emptyMap(),

    // ID del paseador que fue aceptado (solo si estado es CONFIRMADO o EN_CAMINO)
    val paseadorIdAceptado: Int? = null,

    // Precio final acordado (puede ser el costoOfrecido o una contraoferta aceptada)
    val precioFinal: Double? = null
)