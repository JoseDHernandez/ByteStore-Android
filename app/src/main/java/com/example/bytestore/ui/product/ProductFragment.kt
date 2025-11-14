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

class ProductFragment : Fragment() {
    //formater
    val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        // Mostrar precios como enteros (sin decimales) para consistencia con el resto de la app
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    //argumentos
    private val args: ProductFragmentArgs by navArgs()

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()

    // ViewModel del carrito
    private val cartViewModel: CartViewModel by activityViewModels { AppViewModelFactory(requireContext()) }

    // Indica si el producto fue agregado previamente mediante el botón "Añadir al carrito"
    // Esto nos ayuda a distinguir el comportamiento de "Comprar" dependiendo del flujo del usuario.
    private var addedViaAddButton: Boolean = false

    //datos del producto
    private lateinit var product: ProductModel

    // adapter de productos similares
    val productsAdapter = ProductsListAdapter { product ->
        //consultar el producto seleccionado
        getAllData(product.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Producto")
        //obtener id del producto de los argumentos/parametros
        getAllData(args.productId)
        //pintar datos
        setProductLiveData()

        //botón de compra
        binding.buttonBuy.setOnClickListener {
            handleBuyNow()
        }

        binding.buttonAddCart.setOnClickListener {
            handleAddToCart()
        }

        //productos similares
        binding.similarProductsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = productsAdapter
        }
        //espaciado (reautilizado del grid de todos los productos)
        binding.similarProductsRecyclerView.addItemDecoration(
            HorizontalSpaceItemDecoration(
                resources.getDimensionPixelSize(R.dimen.grid_spacing)
            )
        )
        setSimilarProductsLiveData()
    }

    // Función para comprar directamente
    private fun handleBuyNow() {
        if (!::product.isInitialized) {
            Toast.makeText(requireContext(), "Producto no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        if (product.stock <= 0) {
            Toast.makeText(requireContext(), "Producto sin stock disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // CORREGIDO: Calcular precio con descuento correctamente
        // product.price está en PESOS (ej: 3299000.0)
        // product.discount es el porcentaje (ej: 54.0)
        val discountAmount = product.price * (product.discount / 100f)
        val finalPrice = (product.price - discountAmount).toLong()


        // No agregar al carrito: al pulsar "Comprar" se inicia un flujo de pago
        // directo para este producto (compra puntual). Preparar estado temporal
        // en el ViewModel del carrito y navegar al checkout.
        cartViewModel.startCheckoutWithItem(
            productId = product.id.toLong(),
            name = product.name,
            image = product.image,
            unitPricePesos = finalPrice,
            qty = 1
        )

        binding.root.postDelayed({
            try {
                findNavController().navigate(R.id.checkoutFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al navegar al checkout", Toast.LENGTH_SHORT).show()
            }
        }, 200)
    }

    // Función para añadir al carrito
    private fun handleAddToCart() {
        if (!::product.isInitialized) {
            Toast.makeText(requireContext(), "Producto no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        if (product.stock <= 0) {
            Toast.makeText(requireContext(), "Producto sin stock disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // CORREGIDO: Calcular precio con descuento correctamente
        // product.price está en PESOS (ej: 3299000.0)
        // product.discount es el porcentaje (ej: 54.0)
        val discountAmount = product.price * (product.discount / 100f)
        val finalPrice = (product.price - discountAmount).toLong()

        Log.d("ProductFragment", "🛒 Añadiendo al carrito:")
        Log.d("ProductFragment", "  - Producto: ${product.name}")
        Log.d("ProductFragment", "  - Precio original: $${product.price}")
        Log.d("ProductFragment", "  - Descuento: ${product.discount}%")
        Log.d("ProductFragment", "  - Precio final: $${finalPrice}")

        // Agregar al carrito
        cartViewModel.add(
            productId = product.id.toLong(),
            name = product.name,
            image = product.image,
            unitPrice = finalPrice,  // Ya está en PESOS
            qty = 1
        )

        // Marcamos que el usuario añadió el producto mediante el botón "Añadir al carrito"
        addedViaAddButton = true

        // Mostrar confirmación
        Snackbar.make(binding.root, "✓ ${product.name} agregado al carrito", Snackbar.LENGTH_LONG)
            .setAction("VER CARRITO") {
                try {
                    // Navega directamente al cartFragment por ID
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
    }

    //solicitar informacion de producto
    private fun getAllData(id: Int) {
        if (id < 0) {
            findNavController().navigate(R.id.action_productFragment_to_productsFragment)
        }
        viewModel.getProduct(id)
        viewModel.getSimilarProducts(id)
        binding.scrollView.smoothScrollTo(0,0)
    }

    //Datos del producto
    private fun setProductLiveData() {
        viewModel.productState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    //variables del producto
                    product = state.data
                    var url = product.image.replace("localhost", "10.0.2.2")
                    // Asegurar URL absoluta: si el servidor devuelve rutas relativas (p.ej. /uploads/xxx),
                    // prefix con la base configurada en ApiClient.
                    try {
                        val base = com.example.bytestore.core.ApiClient.retrofit(binding.image.context).baseUrl().toString()
                        if (!url.startsWith("http")) {
                            url = base.trimEnd('/') + "/" + url.trimStart('/')
                        }
                    } catch (e: Exception) {
                        // si no se puede obtener la base, continuar con la cadena tal cual
                    }
                    val countScore = 10
                    //asignacion de la información
                    binding.title.text = product.name
                    Glide.with(binding.image.context)
                        .load(url)
                        .override(300, 300)
                        .placeholder(R.drawable.placeholder)
                        .fitCenter()
                        .into(binding.image)
                    binding.price.text = getString(R.string.price_format, formatter.format(product.price))
                    val discountedPrice = product.price - (product.price * product.discount) / 100
                    binding.discountPrice.text = getString(R.string.price_format, formatter.format(discountedPrice))
                    binding.discountPrice.paintFlags = binding.discountPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.discount.text = getString(R.string.discount_percentage, product.discount)
                    binding.score.rating = product.qualification

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
                    //caracteristicas
                    binding.brand.text = product.brand
                    binding.model.text = product.brand
                    binding.operatingSystem.text = product.system.system
                    binding.distribution.text = product.system.distribution
                    //almacenamiento y procesamiento
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
                    //pantalla
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

    //Productos similares
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
