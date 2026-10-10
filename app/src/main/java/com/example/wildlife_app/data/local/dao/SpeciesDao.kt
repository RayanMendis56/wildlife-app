package com.example.wildlife_app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.wildlife_app.data.local.entity.SpeciesEntity

@Dao
interface SpeciesDao {

    @Upsert
    suspend fun upsert(species: SpeciesEntity)

    @Upsert
    suspend fun upsertAll(species: List<SpeciesEntity>)

    @Query("SELECT * FROM species WHERE id = :taxonId")
    suspend fun getById(taxonId: Long): SpeciesEntity?
}