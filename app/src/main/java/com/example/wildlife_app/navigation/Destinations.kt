package com.example.wildlife_app.navigation

object Destinations {
    const val HOME = "home"
    const val SIGHTING_LIST = "sighting_list"
    const val ADD_SIGHTING = "add_sighting"
    const val EDIT_SIGHTING = "edit_sighting/{sightingId}"
    const val SIGHTING_DETAIL = "sighting_detail/{sightingId}"
    const val SETTINGS = "settings"

    fun sightingDetail(id: Long) = "sighting_detail/$id"
    fun editSighting(id: Long) = "edit_sighting/$id"
}