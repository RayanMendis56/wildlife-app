package com.example.wildlife_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wildlife_app.data.local.dao.OutingDao
import com.example.wildlife_app.data.local.dao.SightingDao
import com.example.wildlife_app.data.local.dao.SpeciesDao
import com.example.wildlife_app.data.local.entity.OutingEntity
import com.example.wildlife_app.data.local.entity.SightingEntity
import com.example.wildlife_app.data.local.entity.SpeciesEntity

@Database(
    entities = [SpeciesEntity::class, SightingEntity::class, OutingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun speciesDao(): SpeciesDao
    abstract fun sightingDao(): SightingDao
    abstract fun outingDao(): OutingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wildlife_db"
                ).build().also { INSTANCE = it }
            }
    }
}