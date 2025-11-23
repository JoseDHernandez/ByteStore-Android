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
import com.bumptech.glide.Glide
import com.example.bytestore.data.model.product.DisplayInputs
import com.example.bytestore.data.model.product.ProcessorInputs
import com.example.bytestore.data.model.product.ProductInputs
import com.example.bytestore.data.model.product.SystemInputs
import com.example.bytestore.databinding.FragmentProductRegisterBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.productViewModels.ProductCrudViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar

class ProductRegisterFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"

    private var _binding: FragmentProductRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductCrudViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    // Launcher para seleccionar imagen desde galería
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.productImagePreview)
                binding.imageSelectedLabel.text = "Imagen seleccionada"
            }
        }
    }

    // Launcher para capturar foto desde cámara
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.productImagePreview)
                binding.imageSelectedLabel.text = "Imagen capturada"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Registrar Producto")

        setupButtons()
        setupObservers()
    }

    private fun setupButtons() {
        // Seleccionar imagen
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Tomar foto
        binding.btnTakePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(intent)
        }

        // Guardar producto
        binding.btnSave.setOnClickListener {
            registerProduct()
        }

        // Cancelar
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        // Observar registro de producto
        viewModel.registerProductState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Producto registrado", Toast.LENGTH_SHORT)
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

        // Observar errores de validación
        viewModel.validationErrors.observe(viewLifecycleOwner) { errors ->
            if (errors.isNotEmpty()) {
                showValidationErrors(errors)
            }
        }
    }

    private fun registerProduct() {
        // Limpiar errores anteriores
        clearErrors()

        // Validar que haya imagen seleccionada
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Debe seleccionar una imagen", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Recopilar datos del formulario
        val input = ProductInputs(
            name = binding.inputName.text.toString().trim(),
            description = binding.inputDescription.text.toString().trim(),
            price = binding.inputPrice.text.toString().toDoubleOrNull() ?: 0.0,
            discount = binding.inputDiscount.text.toString().toIntOrNull() ?: 0,
            stock = binding.inputStock.text.toString().toIntOrNull() ?: 0,
            image = "", // Se completará con la URL de la imagen subida
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

        // Registrar producto
        viewModel.registerProduct(input, requireContext(), selectedImageUri)
    }

    private fun showValidationErrors(errors: Map<String, String>) {
        errors.forEach { (field, message) ->
            when (field) {
                "name" -> binding.inputName.error = message
                "description" -> binding.inputDescription.error = message
                "price" -> binding.inputPrice.error = message
                "discount" -> binding.inputDiscount.error = message
                "stock" -> binding.inputStock.error = message
                "image" -> binding.imageSelectedLabel.error = message
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