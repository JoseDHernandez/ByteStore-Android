package com.example.bytestore.utils.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.bytestore.R
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Small helper that wraps Geocoder differences between API levels and produces a user friendly
 * address string. Location fragments call [reverseGeocode] and get the formatted text via the
 * provided callback so UI updates remain on the main thread.
 */
fun Context.reverseGeocode(
    lat: Double,
    lon: Double,
    isApprox: Boolean,
    onResult: (String) -> Unit
) {
    val fallback = formatAsCoordinates(this, lat, lon, isApprox)
    try {
        val geocoder = Geocoder(this, Locale.getDefault())

        // If Geocoder implementation is not present on device, return fallback coordinates
        if (!Geocoder.isPresent()) {
            onResult(fallback)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lon, 1) { addresses ->
                val text = buildAddressText(this, addresses.firstOrNull(), lat, lon, isApprox)
                onResult(text)
            }
        } else {
            // Synchronous API on older devices - run on IO to avoid blocking the UI thread
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val text = buildAddressText(this@reverseGeocode, addresses?.firstOrNull(), lat, lon, isApprox)
                    withContext(Dispatchers.Main) { onResult(text) }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) { onResult(fallback) }
                }
            }
        }
    } catch (_: Exception) {
        // Any unexpected exception -> return fallback
        onResult(fallback)
    }
}

private fun buildAddressText(
    context: Context,
    address: Address?,
    lat: Double,
    lon: Double,
    isApprox: Boolean
): String {
    if (address == null) {
        return formatAsCoordinates(context, lat, lon, isApprox)
    }

    // If address country or locale indicates Colombia, try Colombian-style formatting
    val country = address.countryName?.lowercase(Locale.getDefault())
    val isColombia = country?.contains("colombia") == true || Locale.getDefault().country.equals("CO", ignoreCase = true)

    if (isColombia) {
        // Try to build a Colombian-friendly address: "Calle <thoroughfare> # <subThoroughfare>, <locality> - <adminArea>, Colombia"
        val parts = mutableListOf<String>()

        val streetPart = when {
            !address.thoroughfare.isNullOrBlank() && !address.subThoroughfare.isNullOrBlank() ->
                "${address.thoroughfare} # ${address.subThoroughfare}"
            !address.thoroughfare.isNullOrBlank() -> address.thoroughfare
            else -> null
        }
        if (!streetPart.isNullOrBlank()) parts.add(streetPart)

        // Some Geocoder results include sublocality or feature name that can be neighborhood
        val neighborhood = address.subLocality ?: address.featureName
        if (!neighborhood.isNullOrBlank() && !parts.contains(neighborhood)) parts.add(neighborhood)

        if (!address.locality.isNullOrBlank()) parts.add(address.locality)
        if (!address.adminArea.isNullOrBlank()) parts.add(address.adminArea)

        if (parts.isNotEmpty()) {
            // Join first two as street/neighborhood then city and admin
            return when (parts.size) {
                1 -> parts[0] + ", Colombia"
                2 -> "${parts[0]}, ${parts[1]}, Colombia"
                else -> "${parts[0]}, ${parts[1]} - ${parts[2]}, Colombia"
            }
        }
    }

    val line0 = address.getAddressLine(0)
    if (!line0.isNullOrBlank()) {
        val countryName = address.countryName
        return if (!countryName.isNullOrBlank() && !line0.contains(countryName)) {
            "$line0, $countryName"
        } else {
            line0
        }
    }

    // Fallback to previous logic
    val street = when {
        !address.thoroughfare.isNullOrBlank() && !address.subThoroughfare.isNullOrBlank() ->
            "${address.thoroughfare} # ${address.subThoroughfare}"
        !address.thoroughfare.isNullOrBlank() ->
            address.thoroughfare
        else -> null
    }

    val city = address.locality
    val adminArea = address.adminArea
    val countryName = address.countryName

    val region = when {
        !city.isNullOrBlank() && !adminArea.isNullOrBlank() && !countryName.isNullOrBlank() -> "$city - $adminArea, $countryName"
        !city.isNullOrBlank() && !adminArea.isNullOrBlank() -> "$city - $adminArea"
        !city.isNullOrBlank() && !countryName.isNullOrBlank() -> "$city, $countryName"
        !adminArea.isNullOrBlank() && !countryName.isNullOrBlank() -> "$adminArea, $countryName"
        else -> listOfNotNull(city, adminArea, countryName).joinToString(", ")
    }

    val combined = listOfNotNull(street, region).joinToString(", ")
    if (combined.isNotBlank()) {
        return combined
    }

    return formatAsCoordinates(context, lat, lon, isApprox)
}

private fun formatAsCoordinates(
    context: Context,
    lat: Double,
    lon: Double,
    isApprox: Boolean
): String {
    val format = if (isApprox) R.string.location_coordinates_approx else R.string.location_coordinates_precise
    return context.getString(format, lat, lon)
}
