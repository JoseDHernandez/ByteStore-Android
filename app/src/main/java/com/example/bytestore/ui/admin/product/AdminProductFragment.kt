package com.example.bytestore.ui.admin.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.bytestore.R
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.databinding.FragmentAdminProductBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.product.ProductFragmentArgs
import com.example.bytestore.ui.viewmodel.adminViewModels.ProductCrudViewModel
import com.example.bytestore.ui.viewmodel.productViewModels.ProductViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar
import java.text.NumberFormat
import java.util.Locale


class AdminProductFragment : ProtectedFragment() {
    private var _binding: FragmentAdminProductBinding? = null
    private val binding get() = _binding!!
    private val args: ProductFragmentArgs by navArgs()
    private val viewModel: ProductViewModel by viewModels()
    private val crudViewModel: ProductCrudViewModel by viewModels()
    private lateinit var product: ProductModel
    val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Producto")
        //solicitar datos
        viewModel.getProduct(args.productId)
        binding.scrollView.smoothScrollTo(0, 0)
        setProductLiveData()
        //botones
        binding.updateButton.setOnClickListener {
            val action =
                AdminProductFragmentDirections.actionProductListAdminFragmentToProductEditFragment(
                    product.id
                )
            findNavController().navigate(action)
        }
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation(product.id, product.image, product.name)
        }
    }

    private fun showDeleteConfirmation(productId: Int, imageUrl: String, productName: String) {
        AlertDialog.Builder(requireContext(), R.style.Theme_ByteStore_AlertDialog)
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de eliminar '$productName'?")
            .setPositiveButton("Eliminar") { _, _ ->
                crudViewModel.deleteProduct(productId, imageUrl)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setProductLiveData() {
        viewModel.productState.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
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
                binding.price.text =
                    getString(R.string.price_format, formatter.format(product.price))
                val discountedPrice = product.price - (product.price * product.discount) / 100
                binding.discountPrice.text =
                    getString(R.string.price_format, formatter.format(discountedPrice))

                binding.discount.text = getString(R.string.discount_percentage, product.discount)
                binding.score.text = product.qualification.toString()

                binding.scoreLabel.text =
                    when (countScore) {
                        0 -> "Sin unidades disponibles"
                        1 -> "1 reseña"
                        else -> "$countScore reseñas"
                    }


                binding.stock.text = when (product.stock) {
                    0 -> "Sin unidades disponibles"
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
            } else if (state is Resource.Error) {
                findNavController().navigate(R.id.action_adminProductFragment_to_productsListAdminFragment)
            }

        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}