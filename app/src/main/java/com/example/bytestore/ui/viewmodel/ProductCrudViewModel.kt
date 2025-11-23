package com.example.bytestore.ui.viewmodel.productViewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.product.DisplayModel
import com.example.bytestore.data.model.product.OperatingSystemModel
import com.example.bytestore.data.model.product.ProcessorModel
import com.example.bytestore.data.model.product.ProductInputs
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.ProductRegisterRequest
import com.example.bytestore.data.model.product.ProductUpdateRequest
import com.example.bytestore.data.model.product.ProductValidator
import com.example.bytestore.data.repository.ProductRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductCrudViewModel : ViewModel() {
    private val repository = ProductRepository()

    // Estado del registro de producto
    private val _registerProductState = MutableLiveData<Resource<ProductModel>>(Resource.Idle)
    val registerProductState: LiveData<Resource<ProductModel>> get() = _registerProductState

    // Estado de actualización de producto
    private val _updateProductState = MutableLiveData<Resource<ProductModel>>(Resource.Idle)
    val updateProductState: LiveData<Resource<ProductModel>> get() = _updateProductState

    // Estado de eliminación de producto
    private val _deleteProductState = MutableLiveData<Resource<Boolean>>(Resource.Idle)
    val deleteProductState: LiveData<Resource<Boolean>> get() = _deleteProductState

    // Estado de carga de imagen
    private val _uploadImageState = MutableLiveData<Resource<String>>(Resource.Idle)
    val uploadImageState: LiveData<Resource<String>> get() = _uploadImageState

    // Errores de validación
    private val _validationErrors = MutableLiveData<Map<String, String>>(emptyMap())
    val validationErrors: LiveData<Map<String, String>> get() = _validationErrors

    // Registrar producto
    fun registerProduct(input: ProductInputs, context: Context, imageUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            _registerProductState.postValue(Resource.Loading)

            // Validar inputs
            val errors = ProductValidator.validateProduct(input)
            if (errors.isNotEmpty()) {
                _validationErrors.postValue(errors)
                _registerProductState.postValue(Resource.ValidationError(errors))
                return@launch
            }

            try {
                // Subir imagen si existe
                var imageUrl = input.image
                if (imageUri != null) {
                    when (val imageResult = repository.uploadImage(context, imageUri)) {
                        is Resource.Success -> imageUrl = imageResult.data
                        is Resource.Error -> {
                            _registerProductState.postValue(Resource.Error("Error al subir imagen: ${imageResult.message}"))
                            return@launch
                        }

                        is Resource.ValidationError -> {
                            _registerProductState.postValue(Resource.Error("Error de validación de imagen"))
                            return@launch
                        }

                        else -> {}
                    }
                }

                // Crear request
                val request = ProductRegisterRequest(
                    name = input.name,
                    description = input.description,
                    price = input.price.toFloat(),
                    discount = input.discount.toFloat(),
                    stock = input.stock,
                    image = imageUrl,
                    model = input.model,
                    ramCapacity = input.ramCapacity,
                    diskCapacity = input.diskCapacity,
                    qualification = 0f,
                    brand = input.brand,
                    processor = ProcessorModel(
                        id = null,
                        brand = input.processor.brand,
                        family = input.processor.family,
                        model = input.processor.model,
                        cores = input.processor.cores,
                        speed = input.processor.speed
                    ),
                    system = OperatingSystemModel(
                        id = null,
                        system = input.system.system,
                        distribution = input.system.distribution
                    ),
                    display = DisplayModel(
                        id = null,
                        size = input.display.size.toFloat(),
                        resolution = input.display.resolution,
                        graphics = input.display.graphics,
                        brand = input.display.brand
                    )
                )

                // Registrar producto
                val response = repository.registerProduct(request)
                _registerProductState.postValue(response)

            } catch (e: Exception) {
                e.printStackTrace()
                _registerProductState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    // Actualizar producto
    fun updateProduct(
        id: Int,
        input: ProductInputs,
        context: Context,
        imageUri: Uri?,
        oldImageUrl: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateProductState.postValue(Resource.Loading)

            // Validar inputs
            val errors = ProductValidator.validateProduct(input)
            if (errors.isNotEmpty()) {
                _validationErrors.postValue(errors)
                _updateProductState.postValue(Resource.ValidationError(errors))
                return@launch
            }

            try {
                // Manejar imagen
                var imageUrl = input.image
                if (imageUri != null) {
                    // Si hay una imagen anterior, reemplazarla
                    if (!oldImageUrl.isNullOrBlank()) {
                        val filename = oldImageUrl.substringAfterLast("/")
                        when (val imageResult =
                            repository.changeImage(context, imageUri, filename)) {
                            is Resource.Success -> imageUrl = imageResult.data
                            is Resource.Error -> {
                                _updateProductState.postValue(Resource.Error("Error al actualizar imagen: ${imageResult.message}"))
                                return@launch
                            }

                            else -> {}
                        }
                    } else {
                        // Si no hay imagen anterior, subir nueva
                        when (val imageResult = repository.uploadImage(context, imageUri)) {
                            is Resource.Success -> imageUrl = imageResult.data
                            is Resource.Error -> {
                                _updateProductState.postValue(Resource.Error("Error al subir imagen: ${imageResult.message}"))
                                return@launch
                            }

                            else -> {}
                        }
                    }
                }

                // Crear request de actualización
                val request = ProductUpdateRequest(
                    name = input.name,
                    description = input.description,
                    price = input.price.toFloat(),
                    discount = input.discount.toFloat(),
                    stock = input.stock,
                    image = imageUrl,
                    model = input.model,
                    ramCapacity = input.ramCapacity,
                    diskCapacity = input.diskCapacity,
                    qualification = null,
                    brand = null, // Se debe obtener el ID de la marca
                    processorId = null, // Se debe obtener el ID del procesador
                    systemId = null, // Se debe obtener el ID del sistema
                    displayId = null // Se debe obtener el ID del display
                )

                val response = repository.updateProduct(id, request)
                _updateProductState.postValue(response)

            } catch (e: Exception) {
                e.printStackTrace()
                _updateProductState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    // Eliminar producto
    fun deleteProduct(id: Int, imageUrl: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _deleteProductState.postValue(Resource.Loading)
            try {
                // Eliminar imagen si existe
                if (!imageUrl.isNullOrBlank()) {
                    val filename = imageUrl.substringAfterLast("/")
                    repository.deleteImage(filename)
                }

                // Eliminar producto
                val success = repository.deleteProduct(id)
                if (success) {
                    _deleteProductState.postValue(Resource.Success(true))
                } else {
                    _deleteProductState.postValue(Resource.Error("Error al eliminar producto"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _deleteProductState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    // Subir imagen independiente
    fun uploadImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uploadImageState.postValue(Resource.Loading)
            try {
                val response = repository.uploadImage(context, uri)
                _uploadImageState.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadImageState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    // Limpiar estados
    fun clearStates() {
        _registerProductState.value = Resource.Idle
        _updateProductState.value = Resource.Idle
        _deleteProductState.value = Resource.Idle
        _uploadImageState.value = Resource.Idle
        _validationErrors.value = emptyMap()
    }

    // Limpiar errores de validación
    fun clearValidationErrors() {
        _validationErrors.value = emptyMap()
    }
}