package com.example.wildlife_app.data.remote.dto

import com.example.wildlife_app.domain.model.Species

/**
 * Converts a raw API taxon into a clean Species, or null if it's unusable.
 * This is where the bad-data rules live.
 */
fun TaxonDto.toSpeciesOrNull(): Species? {
    val taxonId = id ?: return null
    val scientific = name?.trim()
    if (scientific.isNullOrEmpty()) return null          // no usable name
    if (rank != "species") return null                   // skip families, genera, etc.

    return Species(
        id = taxonId,
        scientificName = scientific,
        commonName = preferredCommonName?.trim()?.takeIf { it.isNotEmpty() },
        photoUrl = defaultPhoto?.mediumUrl ?: defaultPhoto?.squareUrl
    )
}