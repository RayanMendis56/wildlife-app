package com.example.wildlife_app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.wildlife_app.data.local.entity.SightingEntity
import com.example.wildlife_app.data.local.entity.SpeciesEntity

data class SightingWithSpecies(
    @Embedded val sighting: SightingEntity,
    @Relation(
        parentColumn = "speciesId",
        entityColumn = "id"
    )
    val species: SpeciesEntity
)