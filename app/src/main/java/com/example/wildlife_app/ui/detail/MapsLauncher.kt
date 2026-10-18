package com.example.wildlife_app.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Opens a location in a maps app. Returns false if nothing on the device can handle it. */
fun openInMaps(
    context: Context,
    latitude: Double,
    longitude: Double,
    label: String
): Boolean {
    val geoUri = Uri.parse(
        "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})"
    )
    val webUri = Uri.parse(
        "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    )

    for (uri in listOf(geoUri, webUri)) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            return true
        } catch (_: ActivityNotFoundException) {
        }
    }
    return false
}
