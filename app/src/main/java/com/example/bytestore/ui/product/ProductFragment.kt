package com.example.bytestore.ui.product

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bytestore.R
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.databinding.FragmentProductBinding
import com.example.bytestore.ui.components.HorizontalSpaceItemDecoration
import com.example.bytestore.ui.viewmodel.productViewModels.ProductViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar
import java.text.NumberFormat
import java.util.Locale

class ProductFragment : Fragment() {
    //formater
    val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "CO"))

    //argumentos
    private val args: ProductFragmentArgs by navArgs()

    //unidades a comprar
    private var buyUnits = 0
    private val maxUnits = 1
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()

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

        //boton de compra
        binding.buttonBuy.setOnClickListener {
            //TODO: logica de compra
        }
        binding.buttonAddCart.setOnClickListener {
            //TODO: logica del carrito
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

    private fun shopUnits() {

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
                    val url = product.image.replace("localhost", "10.0.2.2")
                    val countScore = 10
                    //asignacion de la información
                    binding.title.text = product.name
                    Glide.with(binding.image.context)
                        .load(url)
                        .override(300, 300)
                        .placeholder(R.drawable.placeholder)
                        .fitCenter()
                        .into(binding.image)
                    binding.price.text = "$${formatter.format(product.price)}"
                    binding.discountPrice.text =
                        "$${formatter.format(product.price - (product.price * product.discount) / 100)}"
                    binding.discountPrice.paintFlags =
                        binding.discountPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.discount.text = "-${product.discount}%"
                    binding.score.rating = product.qualification
                    //TODO: Pendite obtener cantidad de reseñas
                    binding.scoreLabel.text = when (countScore) {
                        0 -> "(Sin reseñas)"
                        1 -> "(1 reseña)"
                        else -> "(${countScore} reseñas)"
                    }
                    binding.stock.text = when (product.stock) {
                        0 -> "Sin unidades disponibles"
                        1 -> "Ultima unidad disponible"
                        else -> "${product.stock} Unidades disponibles"
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
                    binding.diskCapacity.text =
                        if (product.diskCapacity > 999) "${product.diskCapacity / 100} TB" else "${product.diskCapacity} GB"
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