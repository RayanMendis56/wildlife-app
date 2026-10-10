package com.example.wildlife_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One species. The primary key IS the iNaturalist taxon ID, so the same
 * species can never be stored twice.
 */
@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey val id: Long,          // iNaturalist taxon ID (not auto-generated)
    val scientificName: String,
    val commonName: String?,           // nullable: API sometimes has none
    val photoUrl: String?              // nullable: API sometimes has none
)