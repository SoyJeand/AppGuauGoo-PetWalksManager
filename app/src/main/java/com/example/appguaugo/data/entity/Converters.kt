package com.example.appguaugo.data.entity

import androidx.room.TypeConverter
import com.google.gson.Gson // ¡NECESITAS ESTA IMPORTACIÓN!
import com.google.gson.reflect.TypeToken // ¡NECESITAS ESTA IMPORTACIÓN!
import java.util.Date

class Converters {

    // --- CONVERTIDORES DE DATE (Tus originales) ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // --- NUEVOS CONVERTIDORES PARA CONTRAOFERTAS ---

    private val gson = Gson()

    // Convierte el Map<Int, Double> a String JSON para guardar en la BD
    @TypeConverter
    fun fromMapToString(map: Map<Int, Double>): String {
        return gson.toJson(map)
    }

    // Convierte el String JSON de la BD de vuelta a Map<Int, Double>
    @TypeConverter
    fun fromStringToMap(mapString: String): Map<Int, Double> {
        val type = object : TypeToken<Map<Int, Double>>() {}.type
        return gson.fromJson(mapString, type) ?: emptyMap()
    }
}