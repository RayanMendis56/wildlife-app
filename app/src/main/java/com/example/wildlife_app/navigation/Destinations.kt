
package com.example.wildlife_app.navigation

object Destinations {
    const val HOME = "home"
    const val SIGHTING_LIST = "sighting_list"
    const val OUTINGS = "outings"
    const val OUTING_DETAIL = "outing_detail/{outingId}"
    const val ADD_SIGHTING = "add_sighting"
    const val EDIT_SIGHTING = "edit_sighting/{sightingId}"
    const val SIGHTING_DETAIL = "sighting_detail/{sightingId}"
    const val SETTINGS = "settings"

    fun outingDetail(id: Long): String =
        "outing_detail/$id"

    fun sightingDetail(id: Long): String =
        "sighting_detail/$id"

    fun editSighting(id: Long): String =
        "edit_sighting/$id"
}
