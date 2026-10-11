package com.example.wildlife_app.domain.model

data class Species(
    val id: Long,                  // iNaturalist taxon ID
    val scientificName: String,
    val commonName: String?,
    val photoUrl: String?
) {
    // Used by the UI with the DataStore preference
    fun displayName(preferCommon: Boolean): String =
        if (preferCommon) commonName ?: scientificName else scientificName
}