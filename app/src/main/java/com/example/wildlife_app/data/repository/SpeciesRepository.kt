package com.example.wildlife_app.data.repository

import com.example.wildlife_app.data.local.dao.SpeciesDao
import com.example.wildlife_app.data.local.entity.SpeciesEntity
import com.example.wildlife_app.data.remote.INaturalistApi
import com.example.wildlife_app.data.remote.dto.toSpeciesOrNull
import com.example.wildlife_app.domain.model.Species
import kotlin.coroutines.cancellation.CancellationException

class SpeciesRepository(
    private val api: INaturalistApi,
    private val speciesDao: SpeciesDao
) {

    suspend fun searchSpecies(query: String): Result<List<Species>> = try {
        val response = api.searchTaxa(query.trim())
        val species = response.results
            .mapNotNull { it.toSpeciesOrNull() }   // drop unusable records
            .distinctBy { it.id }                  // drop duplicate taxa
        Result.success(species)
    } catch (e: CancellationException) {
        throw e                                    // never swallow coroutine cancellation
    } catch (e: Exception) {
        Result.failure(e)                          // network or parse failure
    }

    // Called when the user picks a species for a sighting
    suspend fun saveSpecies(species: Species) {
        speciesDao.upsert(
            SpeciesEntity(
                id = species.id,
                scientificName = species.scientificName,
                commonName = species.commonName,
                photoUrl = species.photoUrl
            )
        )
    }

    suspend fun getSavedSpeciesById(taxonId: Long): Species? {
        val entity = speciesDao.getById(taxonId) ?: return null

        return Species(
            id = entity.id,
            scientificName = entity.scientificName,
            commonName = entity.commonName,
            photoUrl = entity.photoUrl
        )
    }
}