package com.example.appguaugo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase // Importación necesaria para la migración

import com.example.appguaugo.data.dao.ClienteDao
import com.example.appguaugo.data.dao.MascotaDao
import com.example.appguaugo.data.dao.PaseadorVerificacionDao
import com.example.appguaugo.data.dao.ReclamoDao
import com.example.appguaugo.data.dao.SolicitudPaseoDao
import com.example.appguaugo.data.entity.ClienteEntity
import com.example.appguaugo.data.entity.Converters // Tu clase que ahora incluye Date y Map converters
import com.example.appguaugo.data.entity.MascotaEntity
import com.example.appguaugo.data.entity.PaseadorVerificacionEntity
import com.example.appguaugo.data.entity.ReclamoEntity
import com.example.appguaugo.data.entity.SolicitudPaseoEntity


@Database(entities = [ClienteEntity:: class,
    MascotaEntity::class,
    SolicitudPaseoEntity::class,
    ReclamoEntity::class,
    PaseadorVerificacionEntity::class],
    version = 11, // <-- ¡VERSIÓN INCREMENTADA DE 8 A 9!
    exportSchema = false)

@TypeConverters(Converters::class)
abstract class GuauDb: RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun mascotaDao(): MascotaDao
    abstract fun solicitudPaseoDao(): SolicitudPaseoDao
    abstract fun reclamoDao(): ReclamoDao
    abstract fun paseadorVerificacionDao(): PaseadorVerificacionDao

}