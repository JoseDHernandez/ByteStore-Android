package com.example.bytestore.ui.order

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bytestore.databinding.FragmentOrderBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class OrderFragment : Fragment() {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            captureLocation()
        } else {
            Toast.makeText(requireContext(), "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.addressButton.setOnClickListener { onAddressButtonClick() }
    }

    private fun onAddressButtonClick() {
        if (!hasLocationPermission()) {
            requestPermissionLauncher.launch(locationPermissions)
        } else {
            captureLocation()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return locationPermissions.any {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isFineGranted(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun captureLocation() {
        if (!hasLocationPermission()) return

        val priority = if (isFineGranted()) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val cts = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(priority, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    handleLocation(location)
                } else {
                    // fallback to last known (may be approximate)
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            if (lastLocation != null) {
                                handleLocation(lastLocation)
                            } else {
                                Toast.makeText(requireContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error obteniendo la ubicación", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error obteniendo la ubicación", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleLocation(location: Location) {
        // Consideramos aproximada si no hay permiso fino o la accuracy es grande
        val approxThresholdMeters = 500f
        val isApprox = !isFineGranted() || (location.hasAccuracy() && location.accuracy > approxThresholdMeters)

        reverseGeocodeAndFill(location.latitude, location.longitude, isApprox)
    }

    private fun reverseGeocodeAndFill(lat: Double, lon: Double, isApprox: Boolean) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    val address = addresses.firstOrNull()
                    val text = buildAddressText(address, lat, lon, isApprox)
                    binding.addressInput.setText(text)
                    binding.addressInput.setSelection(text.length)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val address = addresses?.firstOrNull()
                val text = buildAddressText(address, lat, lon, isApprox)
                binding.addressInput.setText(text)
                binding.addressInput.setSelection(text.length)
            }
        } catch (e: Exception) {
            val text = if (isApprox) String.format(Locale.getDefault(), "Aprox: %.5f, %.5f", lat, lon)
            else String.format(Locale.getDefault(), "%.6f, %.6f", lat, lon)
            binding.addressInput.setText(text)
            binding.addressInput.setSelection(text.length)
        }
    }

    private fun buildAddressText(address: Address?, lat: Double, lon: Double, isApprox: Boolean): String {
        if (address == null) {
            return if (isApprox) String.format(Locale.getDefault(), "Aprox: %.5f, %.5f", lat, lon)
            else String.format(Locale.getDefault(), "%.6f, %.6f", lat, lon)
        }

        // 1) Si hay una línea completa, úsala (suele venir ya bien formateada por país)
        val line0 = address.getAddressLine(0)
        if (!line0.isNullOrBlank()) {
            // Asegurar que incluimos país si no viene
            val country = address.countryName
            return if (!country.isNullOrBlank() && !line0.contains(country)) "$line0, $country" else line0
        }

        // 2) Formato manual Latino (ej. Colombia): "Calle X # Y, Ciudad - Depto, País"
        val street = when {
            !address.thoroughfare.isNullOrBlank() && !address.subThoroughfare.isNullOrBlank() ->
                "${address.thoroughfare} # ${address.subThoroughfare}"
            !address.thoroughfare.isNullOrBlank() -> address.thoroughfare
            else -> null
        }
        val city = address.locality
        val admin = address.adminArea
        val country = address.countryName

        val region = when {
            !city.isNullOrBlank() && !admin.isNullOrBlank() && !country.isNullOrBlank() -> "$city - $admin, $country"
            !city.isNullOrBlank() && !admin.isNullOrBlank() -> "$city - $admin"
            !city.isNullOrBlank() && !country.isNullOrBlank() -> "$city, $country"
            !admin.isNullOrBlank() && !country.isNullOrBlank() -> "$admin, $country"
            else -> listOfNotNull(city, admin, country).joinToString(", ")
        }

        val combined = listOfNotNull(street, region).joinToString(", ")
        if (combined.isNotBlank()) return combined

        // 3) Último recurso: coordenadas
        return if (isApprox) String.format(Locale.getDefault(), "Aprox: %.5f, %.5f", lat, lon)
        else String.format(Locale.getDefault(), "%.6f, %.6f", lat, lon)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}