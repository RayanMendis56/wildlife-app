package com.example.wildlife_app.data.repository

import com.example.wildlife_app.data.local.dao.SightingDao
import com.example.wildlife_app.data.local.entity.SightingEntity
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import kotlinx.coroutines.flow.Flow

class SightingRepository(
    private val sightingDao: SightingDao
) {

    fun observeAllSightings(): Flow<List<SightingWithSpecies>> {
        return sightingDao.getAllWithSpecies()
    }

    fun observeSightingById(
        sightingId: Long
    ): Flow<SightingWithSpecies?> {
        return sightingDao.getWithSpeciesById(sightingId)
    }

    suspend fun getSightingById(
        sightingId: Long
    ): SightingEntity? {
        return sightingDao.getById(sightingId)
    }

    suspend fun insertSighting(
        sighting: SightingEntity
    ): Long {
        return sightingDao.insert(sighting)
    }

    suspend fun updateSighting(
        sighting: SightingEntity
    ) {
        sightingDao.update(sighting)
    }

    suspend fun deleteSighting(
        sighting: SightingEntity
    ) {
        sightingDao.delete(sighting)
    }

    fun observeSightingsByOuting(
        outingId: Long
    ): Flow<List<SightingWithSpecies>> {
        return sightingDao.getByOuting(outingId)
    }
}