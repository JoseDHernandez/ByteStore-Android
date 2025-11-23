package com.example.bytestore.ui.cart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentCheckoutBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.CartViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.location.reverseGeocode
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.bytestore.utils.centsToMoney

class CheckoutFragment : ProtectedFragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val vm: CartViewModel by activityViewModels { AppViewModelFactory(requireContext()) }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val grantedFine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val grantedCoarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (grantedFine || grantedCoarse) {
                fetchLocation(grantedFine)
            } else {
                // Determine if any permission was permanently denied (user selected "Don't ask again")
                val fineDenied = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == false
                val coarseDenied = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == false
                val permanentlyDenied = (fineDenied && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                        (coarseDenied && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))

                if (permanentlyDenied) {
                    // Show dialog directing user to app settings
                    showPermissionSettingsDialog()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCart()
        setupClickListeners()
        setupDeliveryMethodListeners()
        setupPaymentMethodListeners()
    }

    private fun observeCart() {
        vm.state.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val cartState = resource.data

                    // Si el carrito está vacío, regresar
                    if (cartState.items.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "El carrito está vacío",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                        return@observe
                    }

                    // Mostrar los totales
                    // Mostrar subtotales sin decimales (pesos enteros)
                    binding.txtSubtotal.text = centsToMoney(cartState.subtotal, showCents = false)
                    updateShippingCost()
                    updateTotal()
                }

                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Loading -> {
                    // Mostrar loading si es necesario
                }

                else -> {}
            }
        }
    }

    private fun setupDeliveryMethodListeners() {
        // Listener para el RadioGroup de método de entrega
        binding.rgDeliveryMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbHomeDelivery -> {
                    // Mostrar sección de dirección
                    binding.txtAddress.isEnabled = true
                    binding.btnEditAddress.isEnabled = true
                    // Actualizar costo de envío
                    updateShippingCost()
                    updateTotal()
                }

                R.id.rbStorePickup -> {
                    // Ocultar sección de dirección
                    binding.txtAddress.isEnabled = false
                    binding.btnEditAddress.isEnabled = false
                    // Envío gratis
                    updateShippingCost()
                    updateTotal()
                }
            }
        }

        // Algunos RadioButtons están dentro de layouts, por eso RadioGroup no los gestiona automáticamente.
        // Añadimos listeners manuales para asegurar que la selección cambie y se actualice la UI.
        binding.rbHomeDelivery.setOnClickListener {
            binding.rbHomeDelivery.isChecked = true
            binding.rbStorePickup.isChecked = false
            binding.txtAddress.isEnabled = true
            binding.btnEditAddress.isEnabled = true
            updateShippingCost()
            updateTotal()
        }

        binding.rbStorePickup.setOnClickListener {
            binding.rbHomeDelivery.isChecked = false
            binding.rbStorePickup.isChecked = true
            binding.txtAddress.isEnabled = false
            binding.btnEditAddress.isEnabled = false
            updateShippingCost()
            updateTotal()
        }
    }

    private fun setupPaymentMethodListeners() {
        // Listener para el RadioGroup de método de pago
        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCash -> {
                    // Mostrar "Contra entrega" solo con pago en efectivo
                    binding.txtPaymentLink.isVisible = true
                }

                R.id.rbCreditCard, R.id.rbPSE -> {
                    // Ocultar "Contra entrega" con otros métodos
                    binding.txtPaymentLink.isVisible = false
                }
            }
        }

        // Listeners manuales en caso de que los RadioButtons no sean manejados por RadioGroup
        binding.rbCreditCard.setOnClickListener {
            binding.rbCreditCard.isChecked = true
            binding.rbPSE.isChecked = false
            binding.rbCash.isChecked = false
            binding.txtPaymentLink.isVisible = false
        }

        binding.rbPSE.setOnClickListener {
            binding.rbCreditCard.isChecked = false
            binding.rbPSE.isChecked = true
            binding.rbCash.isChecked = false
            binding.txtPaymentLink.isVisible = false
        }

        binding.rbCash.setOnClickListener {
            binding.rbCreditCard.isChecked = false
            binding.rbPSE.isChecked = false
            binding.rbCash.isChecked = true
            binding.txtPaymentLink.isVisible = true
        }
    }

    private fun updateShippingCost() {
        val cartState = (vm.state.value as? Resource.Success)?.data ?: return
        // El RadioGroup no controla RadioButtons anidados correctamente, por eso comprobamos
        // directamente el estado de los RadioButtons.
        val shippingCost: Long = if (binding.rbHomeDelivery.isChecked) {
            cartState.shipping
        } else {
            0L
        }

        binding.txtShipping.text = centsToMoney(shippingCost, showCents = false)
    }

    private fun updateTotal() {
        val cartState = (vm.state.value as? Resource.Success)?.data ?: return
        // Calcular envío consultando directamente el estado de los RadioButtons
        val shippingCost: Long = if (binding.rbHomeDelivery.isChecked) cartState.shipping else 0L

        val total = cartState.subtotal + shippingCost
        binding.txtTotal.text = centsToMoney(total, showCents = false)
    }

    private fun setupClickListeners() {
        // Nota: el topBar del Activity maneja el botón "back" (evitamos header duplicado)

        // Botón de confirmar compra
        binding.btnConfirmPurchase.setOnClickListener {
            confirmPurchase()
        }

        // Botón de editar dirección -> Detectar ubicación y autocompletar
        binding.btnEditAddress.setOnClickListener {
            if (!binding.rbHomeDelivery.isChecked) {
                Toast.makeText(requireContext(), "La ubicación aplica solo para envío a domicilio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startLocationFlow()
        }
    }

    private fun confirmPurchase() {
        val currentState = (vm.state.value as? Resource.Success)?.data
        if (currentState == null) {
            Toast.makeText(
                requireContext(),
                "Error al procesar la compra",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Obtener método de entrega seleccionado (comprobamos directamente los RadioButtons)
        val deliveryMethod = when {
            binding.rbHomeDelivery.isChecked -> "Envío a domicilio"
            binding.rbStorePickup.isChecked -> "Recoger en tienda"
            else -> "No especificado"
        }

        // Obtener método de pago seleccionado (comprobamos directamente los RadioButtons)
        val paymentMethod = when {
            binding.rbCreditCard.isChecked -> "Tarjeta"
            binding.rbPSE.isChecked -> "PSE"
            binding.rbCash.isChecked -> "Efectivo"
            else -> "No especificado"
        }

        // Calcular total final
        val shippingCost: Long = if (binding.rbHomeDelivery.isChecked) currentState.shipping else 0L
        val finalTotal = currentState.subtotal + shippingCost

        // TODO: Implementar lógica de compra real (llamar al backend)
        // Aquí deberías enviar:
        // - deliveryMethod
        // - paymentMethod
        // - Dirección de entrega (si es envío a domicilio)
        // - Items del carrito
        // - Total

        Toast.makeText(
            requireContext(),
            "Compra confirmada\nEntrega: $deliveryMethod\nPago: $paymentMethod\nTotal: ${
                centsToMoney(
                    finalTotal,
                    showCents = false
                )
            }",
            Toast.LENGTH_LONG
        ).show()

        // Limpiar el carrito después de la compra
        vm.clear()
        // Si estábamos en un checkout temporal, finalizarlo para que el estado real se recalcule
        vm.finishTemporaryCheckout()

        // Navegar a productos después de la compra
        findNavController().navigate(R.id.action_checkout_to_products)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---- Geolocalización mínima ----
    private fun startLocationFlow() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            // Check that location providers are enabled before fetching
            if (isLocationEnabled()) {
                fetchLocation(fineGranted)
            } else {
                // Prompt user to enable location (GPS)
                showEnableLocationDialog()
            }
            return
        }

        // If we should show a rationale, show it first
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            showPermissionRationaleDialog()
            return
        }

        // Otherwise, request permissions directly
        requestLocationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun isLocationEnabled(): Boolean {
        val lm = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        return try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            false
        }
    }

    private fun fetchLocation(useFine: Boolean) {
        binding.txtAddress.setText(getString(R.string.location_fetching))
        binding.progressAddress.visibility = android.view.View.VISIBLE
        val priority = if (useFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        try {
            fusedLocationClient.getCurrentLocation(priority, null)
                .addOnSuccessListener { location ->
                    binding.progressAddress.visibility = android.view.View.GONE
                    if (location == null) {
                        binding.txtAddress.setText(getString(R.string.location_not_available))
                        return@addOnSuccessListener
                    }
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.d("LocationFlow", "Got location: $lat, $lon (accuracy=${location.accuracy})")
                    // Intentar geocodificación
                    requireContext().reverseGeocode(lat, lon, !useFine) { formatted ->
                        Log.d("LocationFlow", "ReverseGeocode result: $formatted")
                        // Normalize the address for Colombian conventions (helper inside LocationFormatter)
                        val normalized = formatted
                        binding.txtAddress.setText(normalized)
                    }
                }
                .addOnFailureListener { _: Exception ->
                    binding.progressAddress.visibility = android.view.View.GONE
                    binding.txtAddress.setText(getString(R.string.location_not_available))
                }
        } catch (e: SecurityException) {
            binding.progressAddress.visibility = android.view.View.GONE
            binding.txtAddress.setText(getString(R.string.location_permission_denied))
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.location_permission_denied))
            .setMessage("Necesitamos acceder a tu ubicación para autocompletar la dirección de envío. ¿Deseas permitirlo?")
            .setPositiveButton("Permitir") { _, _ ->
                requestLocationPermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.location_permission_denied))
            .setMessage("El permiso de ubicación fue denegado permanentemente. Abre los ajustes de la app y habilítalo manualmente.")
            .setPositiveButton("Abrir ajustes") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Activar ubicación")
            .setMessage("El GPS está desactivado. Actívalo para obtener una ubicación más precisa.")
            .setPositiveButton("Abrir ajustes") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}