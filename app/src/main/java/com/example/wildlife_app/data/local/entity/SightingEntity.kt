package com.example.wildlife_app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sightings",
    foreignKeys = [
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.RESTRICT   // can't delete a species that has sightings
        ),
        ForeignKey(
            entity = OutingEntity::class,
            parentColumns = ["id"],
            childColumns = ["outingId"],
            onDelete = ForeignKey.SET_NULL   // deleting an outing keeps its sightings
        )
    ],
    indices = [Index("speciesId"), Index("outingId")]
)
data class SightingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val speciesId: Long,
    val outingId: Long? = null,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,               // epoch millis
    val photoUri: String? = null,
    val notes: String? = null
)