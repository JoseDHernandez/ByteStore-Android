package com.example.bytestore.ui.product

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bytestore.R
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.databinding.FragmentProductBinding
import com.example.bytestore.ui.components.HorizontalSpaceItemDecoration
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.CartViewModel
import com.example.bytestore.ui.viewmodel.productViewModels.ProductViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

class ProductFragment : Fragment() {
    val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val args: ProductFragmentArgs by navArgs()
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels {
        AppViewModelFactory(
            requireContext()
        )
    }

    private var addedViaAddButton: Boolean = false
    private lateinit var product: ProductModel

    // Variable para almacenar la cantidad seleccionada
    private var selectedQuantity: Int = 1

    val productsAdapter = ProductsListAdapter { product ->
        getAllData(product.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Producto")
        getAllData(args.productId)
        setProductLiveData()

        // Configurar listeners para cantidad
        setupQuantityControls()

        // Botones de compra
        binding.buttonBuy.setOnClickListener {
            handleBuyNow()
        }

        binding.buttonAddCart.setOnClickListener {
            handleAddToCart()
        }

        // Productos similares
        binding.similarProductsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = productsAdapter
        }
        binding.similarProductsRecyclerView.addItemDecoration(
            HorizontalSpaceItemDecoration(
                resources.getDimensionPixelSize(R.dimen.grid_spacing)
            )
        )
        setSimilarProductsLiveData()
    }

    /**
     * Configura los botones de incremento/decremento de cantidad
     */
    private fun setupQuantityControls() {
        // Inicializar cantidad en 1
        selectedQuantity = 1
        updateQuantityDisplay()

        // Botón para decrementar (buttonUnitsLess)
        binding.buttonUnitsLess.setOnClickListener {
            if (selectedQuantity > 1) {
                selectedQuantity--
                updateQuantityDisplay()
            } else {
                Toast.makeText(
                    requireContext(),
                    "La cantidad mínima es 1",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Botón para incrementar (buttonUnitsPlus)
        binding.buttonUnitsPlus.setOnClickListener {
            // Validar que no exceda el stock disponible
            if (::product.isInitialized && selectedQuantity < product.stock) {
                selectedQuantity++
                updateQuantityDisplay()
            } else if (::product.isInitialized) {
                Toast.makeText(
                    requireContext(),
                    "Stock máximo: ${product.stock}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Permitir edición directa en el campo de texto (units)
        binding.units.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // Cuando pierde el foco, validar el valor
                val input = binding.units.text.toString().toIntOrNull() ?: 1
                selectedQuantity = when {
                    input < 1 -> 1
                    ::product.isInitialized && input > product.stock -> product.stock
                    else -> input
                }
                updateQuantityDisplay()
            }
        }
    }

    /**
     * Actualiza la visualización de la cantidad
     */
    private fun updateQuantityDisplay() {
        binding.units.setText(selectedQuantity.toString())
        Log.d("ProductFragment", "Cantidad actualizada: $selectedQuantity")
    }

    private fun handleBuyNow() {
        if (!::product.isInitialized) {
            Toast.makeText(requireContext(), "Producto no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        if (product.stock <= 0) {
            Toast.makeText(requireContext(), "Producto sin stock disponible", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val discountAmount = product.price * (product.discount / 100f)
        val finalPrice = (product.price - discountAmount).toLong()

        cartViewModel.startCheckoutWithItem(
            productId = product.id.toLong(),
            name = product.name,
            image = product.image,
            unitPricePesos = finalPrice,
            qty = selectedQuantity  // Usar la cantidad seleccionada
        )

        binding.root.postDelayed({
            try {
                findNavController().navigate(R.id.checkoutFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al navegar al checkout", Toast.LENGTH_SHORT)
                    .show()
            }
        }, 200)
    }

    private fun handleAddToCart() {
        if (!::product.isInitialized) {
            Toast.makeText(requireContext(), "Producto no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        if (product.stock <= 0) {
            Toast.makeText(requireContext(), "Producto sin stock disponible", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val discountAmount = product.price * (product.discount / 100f)
        val finalPrice = (product.price - discountAmount).toLong()

        Log.d("ProductFragment", "🛒 Añadiendo al carrito:")
        Log.d("ProductFragment", "  - Producto: ${product.name}")
        Log.d("ProductFragment", "  - Precio original: $${product.price}")
        Log.d("ProductFragment", "  - Descuento: ${product.discount}%")
        Log.d("ProductFragment", "  - Precio final: $${finalPrice}")
        Log.d("ProductFragment", "  - Cantidad: $selectedQuantity")

        // Agregar al carrito con la cantidad seleccionada
        cartViewModel.add(
            productId = product.id.toLong(),
            name = product.name,
            image = product.image,
            unitPrice = finalPrice,
            qty = selectedQuantity  // Usar la cantidad seleccionada
        )

        addedViaAddButton = true

        Snackbar.make(
            binding.root,
            "✓ ${product.name} x$selectedQuantity agregado al carrito",
            Snackbar.LENGTH_LONG
        )
            .setAction("VER CARRITO") {
                try {
                    findNavController().navigate(R.id.cartFragment)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Producto agregado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()

        // Resetear cantidad a 1 después de agregar
        selectedQuantity = 1
        updateQuantityDisplay()
    }

    private fun getAllData(id: Int) {
        if (id < 0) {
            findNavController().navigate(R.id.action_productFragment_to_productsFragment)
        }
        viewModel.getProduct(id)
        viewModel.getSimilarProducts(id)
        binding.scrollView.smoothScrollTo(0, 0)
    }

    private fun setProductLiveData() {
        viewModel.productState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    product = state.data
                    val url = product.image.replace("localhost", "10.0.2.2")
                    val countScore = 10
                    binding.title.text = product.name
                    Glide.with(binding.image.context)
                        .load(url)
                        .override(300, 300)
                        .placeholder(R.drawable.placeholder)
                        .fitCenter()
                        .into(binding.image)
                    val price = getString(R.string.price_format, formatter.format(product.price))
                    val discount = product.price - (product.price * product.discount) / 100
                    val discountedPrice =
                        getString(R.string.price_format, formatter.format(discount))
                    //mostra descuento
                    binding.price.text =
                        if (product.discount.toDouble() == 0.0) price else discountedPrice
                    binding.discountPrice.text = price
                    binding.discountPrice.paintFlags =
                        binding.discountPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.discount.text =
                        getString(R.string.discount_percentage, product.discount)
                     binding.score.rating = product.qualification
                   // binding.score.rating = Random.nextDouble(3.0, 5.0).toFloat()
                    //ocultar descuento
                    if (product.discount.toDouble() == 0.0) {
                        binding.discount.visibility = View.INVISIBLE
                        binding.discountPrice.visibility = View.GONE
                    }
                    binding.scoreLabel.text = when (countScore) {
                        0 -> getString(R.string.reviews_none)
                        1 -> getString(R.string.reviews_single)
                        else -> getString(R.string.reviews_multiple, countScore)
                    }

                    binding.stock.text = when (product.stock) {
                        0 -> getString(R.string.stock_none)
                        1 -> getString(R.string.stock_single)
                        else -> getString(R.string.stock_multiple, product.stock)
                    }
                    binding.description.text = product.description
                    binding.brand.text = product.brand
                    binding.model.text = product.brand
                    binding.operatingSystem.text = product.system.system
                    binding.distribution.text = product.system.distribution
                    binding.processorBrand.text = product.processor.brand
                    binding.processorSeries.text = product.processor.family
                    binding.processorModel.text = product.processor.model
                    binding.processorCores.text = product.processor.cores.toString()
                    binding.processorSpeed.text = product.processor.speed
                    binding.diskCapacity.text = if (product.diskCapacity > 999) {
                        getString(R.string.storage_tb, (product.diskCapacity / 1000).toFloat())
                    } else {
                        getString(R.string.storage_gb, product.diskCapacity.toFloat())
                    }
                    binding.ramCapacity.text = product.ramCapacity.toString()
                    binding.displaySize.text = product.display.size.toString()
                    binding.displayResolution.text = product.display.resolution
                    binding.displayGraphics.text = product.display.graphics
                    binding.displayBrand.text = product.display.brand
                }

                is Resource.Idle -> Unit
                is Resource.Loading -> Unit
                is Resource.ValidationError -> Unit
                is Resource.Error -> Unit
            }
        }
    }

    private fun setSimilarProductsLiveData() {
        viewModel.similarProductsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Idle -> Unit
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    productsAdapter.submitList(state.data)
                }

                is Resource.ValidationError -> Unit
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
