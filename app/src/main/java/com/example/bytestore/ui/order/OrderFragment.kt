package com.example.bytestore.ui.order

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bytestore.utils.location.reverseGeocode
import com.example.bytestore.databinding.FragmentOrderBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

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
            Toast.makeText(requireContext(), "Permisos de ubicación denegados", Toast.LENGTH_SHORT)
                .show()
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

        // Read orderId argument and populate UI (sample data)
        val orderId = arguments?.getString("orderId")
        if (!orderId.isNullOrEmpty()) {
            // For demo, we'll set the id and other sample values; in real app, fetch order by id
            binding.tvOrderId.text = "Id: $orderId"
            binding.tvOrderDate.text = "Fecha: 08 Oct 2025"
            binding.tvOrderDeliveredDate.text = "Fecha de entrega: 10 Oct 2025"
            binding.tvPaymentMethod.text = "Método de pago: Tarjeta visa (***** 4235)"
            binding.subtotalPrice.text = "$ 7.991.480"
            binding.deliveryPriceText.text = "$ 15.000"
            val total = "$ 8.006.480"
            binding.totalPrice.text = total
            // fill the top summary total too (tvSummaryTotal)
            binding.tvSummaryTotal.text = total
            binding.addressInput.setText("Calle 45 # 22 - 16")
        }
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
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isFineGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun captureLocation() {
        if (!hasLocationPermission()) return

        val priority =
            if (isFineGranted()) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
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
                                Toast.makeText(
                                    requireContext(),
                                    "Ubicación no disponible",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Error obteniendo la ubicación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Error obteniendo la ubicación",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun handleLocation(location: Location) {
        // Consideramos aproximada si no hay permiso fino o la accuracy es grande
        val approxThresholdMeters = 500f
        val isApprox =
            !isFineGranted() || (location.hasAccuracy() && location.accuracy > approxThresholdMeters)

        // Use centralized reverseGeocode helper to format addresses (Colombian style when possible)
        requireContext().reverseGeocode(location.latitude, location.longitude, isApprox) { text ->
            binding.addressInput.setText(text)
            binding.addressInput.setSelection(text.length)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}