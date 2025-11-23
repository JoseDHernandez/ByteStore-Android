package com.example.bytestore.utils

import android.content.Context
import com.example.bytestore.R

/**
 * Utility object for formatting location coordinates (latitude/longitude).
 *
 * Provides consistent formatting of coordinates throughout the app,
 * supporting both precise and approximate display modes.
 */
object LocationFormatter {
    /**
     * Formats latitude and longitude coordinates into a human-readable string.
     *
     * @param context The Android context for accessing string resources
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param precise If true, displays 6 decimal places (precise).
     *                If false, displays 5 decimal places with "Aprox:" prefix (approximate).
     * @return Formatted coordinate string
     *
     * Example outputs:
     * - precise=true:  "4.711012, -74.072532"
     * - precise=false: "Aprox: 4.71101, -74.07253"
     */
    fun format(context: Context, latitude: Double, longitude: Double, precise: Boolean = true): String {
        return if (precise) {
            context.getString(R.string.location_coordinates_precise, latitude, longitude)
        } else {
            context.getString(R.string.location_coordinates_approx, latitude, longitude)
        }
    }
}
