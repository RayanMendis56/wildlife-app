package com.example.wildlife_app.domain

import kotlin.math.abs

/**
 * Parses coordinates typed by the user.
 * Accepts: -1.2833 | 1.2833 S | 1° 17' S | 1°17'30" S
 */
object CoordinateParser {

    private val pattern = Regex(
        """^(-?\d+(?:\.\d+)?)\s*°?\s*""" +
            """(?:(\d+(?:\.\d+)?)\s*['′’]\s*)?""" +
            """(?:(\d+(?:\.\d+)?)\s*(?:["″”]|''|′′)\s*)?""" +
            """([A-Za-z])?$"""
    )

    /**
     * @param hemispheres allowed letters: "NS" for latitude, "EW" for longitude
     * @return decimal degrees, or null if the text isn't a valid coordinate
     */
    fun parse(input: String, hemispheres: String): Double? {
        val text = input.trim()
        if (text.isEmpty()) return null

        text.toDoubleOrNull()?.let { return it }

        val match = pattern.matchEntire(text) ?: return null
        val degreesText = match.groupValues[1]
        val degrees = degreesText.toDoubleOrNull() ?: return null
        val minutes = match.groupValues[2].toDoubleOrNull() ?: 0.0
        val seconds = match.groupValues[3].toDoubleOrNull() ?: 0.0
        val hemisphere = match.groupValues[4].uppercase()

        if (minutes >= 60.0 || seconds >= 60.0) return null
        if (hemisphere.isNotEmpty() && hemisphere !in hemispheres.map { it.toString() }) {
            return null
        }

        val value = abs(degrees) + minutes / 60.0 + seconds / 3600.0
        val negative = degreesText.startsWith("-") || hemisphere == "S" || hemisphere == "W"
        return if (negative) -value else value
    }
}
