package com.example.wildlife_app.data.remote

import com.example.wildlife_app.data.remote.dto.TaxonSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface INaturalistApi {

    @GET("taxa")
    suspend fun searchTaxa(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 30
    ): TaxonSearchResponse
}