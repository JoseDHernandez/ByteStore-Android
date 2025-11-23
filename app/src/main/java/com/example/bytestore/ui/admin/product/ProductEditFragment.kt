package com.example.bytestore.ui.admin.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.bytestore.R
import com.example.bytestore.data.model.product.DisplayInputs
import com.example.bytestore.data.model.product.ProcessorInputs
import com.example.bytestore.data.model.product.ProductInputs
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.SystemInputs
import com.example.bytestore.databinding.FragmentProductEditBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.productViewModels.ProductCrudViewModel
import com.example.bytestore.ui.viewmodel.productViewModels.ProductViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar

class ProductEditFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"

    private var _binding: FragmentProductEditBinding? = null
    private val binding get() = _binding!!

    private val args: ProductEditFragmentArgs by navArgs()
    private val viewModel: ProductViewModel by viewModels()
    private val crudViewModel: ProductCrudViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var currentProduct: ProductModel? = null

    // Launcher para seleccionar imagen
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.productImagePreview)
                binding.imageSelectedLabel.text = "Nueva imagen seleccionada"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Editar Producto")

        setupButtons()
        setupObservers()

        // Cargar datos del producto
        viewModel.getProduct(args.productId)
    }

    private fun setupButtons() {
        // Seleccionar imagen
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Guardar cambios
        binding.btnSave.setOnClickListener {
            updateProduct()
        }

        // Cancelar
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        // Observar carga del producto
        viewModel.productState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    currentProduct = state.data
                    fillFormWithProductData(state.data)
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }

        // Observar actualización del producto
        crudViewModel.updateProductState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                is Resource.ValidationError -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    showValidationErrors(state.errors)
                }

                else -> Unit
            }
        }
    }

    private fun fillFormWithProductData(product: ProductModel) {
        // Datos básicos
        binding.inputName.setText(product.name)
        binding.inputDescription.setText(product.description)
        binding.inputPrice.setText(product.price.toString())
        binding.inputDiscount.setText(product.discount.toString())
        binding.inputStock.setText(product.stock.toString())
        binding.inputModel.setText(product.model)
        binding.inputRam.setText(product.ramCapacity.toString())
        binding.inputDisk.setText(product.diskCapacity.toString())
        binding.inputBrand.setText(product.brand)

        // Procesador
        binding.inputProcessorBrand.setText(product.processor.brand)
        binding.inputProcessorFamily.setText(product.processor.family)
        binding.inputProcessorModel.setText(product.processor.model)
        binding.inputProcessorCores.setText(product.processor.cores.toString())
        binding.inputProcessorSpeed.setText(product.processor.speed)

        // Sistema operativo
        binding.inputSystem.setText(product.system.system)
        binding.inputDistribution.setText(product.system.distribution)

        // Display
        binding.inputDisplaySize.setText(product.display.size.toString())
        binding.inputDisplayResolution.setText(product.display.resolution)
        binding.inputDisplayGraphics.setText(product.display.graphics)
        binding.inputDisplayBrand.setText(product.display.brand)

        // Imagen actual
        val url = product.image.replace("localhost", "10.0.2.2")
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.placeholder)
            .into(binding.productImagePreview)
        binding.imageSelectedLabel.text = "Imagen actual"
    }

    private fun updateProduct() {
        clearErrors()

        val product = currentProduct ?: return

        // Recopilar datos del formulario
        val input = ProductInputs(
            name = binding.inputName.text.toString().trim(),
            description = binding.inputDescription.text.toString().trim(),
            price = binding.inputPrice.text.toString().toDoubleOrNull() ?: 0.0,
            discount = binding.inputDiscount.text.toString().toIntOrNull() ?: 0,
            stock = binding.inputStock.text.toString().toIntOrNull() ?: 0,
            image = product.image, // Mantener imagen actual si no se cambia
            model = binding.inputModel.text.toString().trim(),
            ramCapacity = binding.inputRam.text.toString().toIntOrNull() ?: 0,
            diskCapacity = binding.inputDisk.text.toString().toIntOrNull() ?: 0,
            processor = ProcessorInputs(
                brand = binding.inputProcessorBrand.text.toString().trim(),
                family = binding.inputProcessorFamily.text.toString().trim(),
                model = binding.inputProcessorModel.text.toString().trim(),
                cores = binding.inputProcessorCores.text.toString().toIntOrNull() ?: 0,
                speed = binding.inputProcessorSpeed.text.toString().trim()
            ),
            system = SystemInputs(
                system = binding.inputSystem.text.toString().trim(),
                distribution = binding.inputDistribution.text.toString().trim()
            ),
            display = DisplayInputs(
                size = binding.inputDisplaySize.text.toString().toIntOrNull() ?: 0,
                resolution = binding.inputDisplayResolution.text.toString().trim(),
                graphics = binding.inputDisplayGraphics.text.toString().trim(),
                brand = binding.inputDisplayBrand.text.toString().trim()
            ),
            brand = binding.inputBrand.text.toString().trim()
        )

        // Actualizar producto
        crudViewModel.updateProduct(
            product.id,
            input,
            requireContext(),
            selectedImageUri,
            product.image
        )
    }

    private fun showValidationErrors(errors: Map<String, String>) {
        errors.forEach { (field, message) ->
            when (field) {
                "name" -> binding.inputName.error = message
                "description" -> binding.inputDescription.error = message
                "price" -> binding.inputPrice.error = message
                "discount" -> binding.inputDiscount.error = message
                "stock" -> binding.inputStock.error = message
                "model" -> binding.inputModel.error = message
                "ram_capacity" -> binding.inputRam.error = message
                "disk_capacity" -> binding.inputDisk.error = message
                "brand" -> binding.inputBrand.error = message
                "processor.brand" -> binding.inputProcessorBrand.error = message
                "processor.family" -> binding.inputProcessorFamily.error = message
                "processor.model" -> binding.inputProcessorModel.error = message
                "processor.cores" -> binding.inputProcessorCores.error = message
                "processor.speed" -> binding.inputProcessorSpeed.error = message
                "system.system" -> binding.inputSystem.error = message
                "system.distribution" -> binding.inputDistribution.error = message
                "display.size" -> binding.inputDisplaySize.error = message
                "display.resolution" -> binding.inputDisplayResolution.error = message
                "display.graphics" -> binding.inputDisplayGraphics.error = message
                "display.brand" -> binding.inputDisplayBrand.error = message
            }
        }
    }

    private fun clearErrors() {
        binding.inputName.error = null
        binding.inputDescription.error = null
        binding.inputPrice.error = null
        binding.inputDiscount.error = null
        binding.inputStock.error = null
        binding.inputModel.error = null
        binding.inputRam.error = null
        binding.inputDisk.error = null
        binding.inputBrand.error = null
        binding.inputProcessorBrand.error = null
        binding.inputProcessorFamily.error = null
        binding.inputProcessorModel.error = null
        binding.inputProcessorCores.error = null
        binding.inputProcessorSpeed.error = null
        binding.inputSystem.error = null
        binding.inputDistribution.error = null
        binding.inputDisplaySize.error = null
        binding.inputDisplayResolution.error = null
        binding.inputDisplayGraphics.error = null
        binding.inputDisplayBrand.error = null
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}