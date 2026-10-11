package com.example.wildlife_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaxonSearchResponse(
    @SerialName("total_results") val totalResults: Int = 0,
    val results: List<TaxonDto> = emptyList()
)

@Serializable
data class TaxonDto(
    val id: Long? = null,
    val name: String? = null,                       // scientific name
    val rank: String? = null,                       // "species", "family", ...
    @SerialName("preferred_common_name") val preferredCommonName: String? = null,
    @SerialName("default_photo") val defaultPhoto: PhotoDto? = null
)

@Serializable
data class PhotoDto(
    @SerialName("square_url") val squareUrl: String? = null,
    @SerialName("medium_url") val mediumUrl: String? = null
)