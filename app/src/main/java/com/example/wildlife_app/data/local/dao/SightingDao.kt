package com.example.wildlife_app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.wildlife_app.data.local.entity.SightingEntity
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import kotlinx.coroutines.flow.Flow

@Dao
interface SightingDao {

    @Insert
    suspend fun insert(sighting: SightingEntity): Long

    @Update
    suspend fun update(sighting: SightingEntity)

    @Delete
    suspend fun delete(sighting: SightingEntity)

    // List screen: every sighting joined with its species, newest first
    @Transaction
    @Query("SELECT * FROM sightings ORDER BY timestamp DESC")
    fun getAllWithSpecies(): Flow<List<SightingWithSpecies>>

    // Detail screen: one sighting by the ID passed through navigation
    @Transaction
    @Query("SELECT * FROM sightings WHERE id = :sightingId")
    fun getWithSpeciesById(sightingId: Long): Flow<SightingWithSpecies?>

    // Edit screen: load once to pre-fill the form
    @Query("SELECT * FROM sightings WHERE id = :sightingId")
    suspend fun getById(sightingId: Long): SightingEntity?

    // Outing grouping
    @Transaction
    @Query("SELECT * FROM sightings WHERE outingId = :outingId ORDER BY timestamp DESC")
    fun getByOuting(outingId: Long): Flow<List<SightingWithSpecies>>
}