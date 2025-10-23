package com.example.bytestore.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.product.ListProductsModels
import com.example.bytestore.data.repository.ProductRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    // Estado de productos con Resource
    private val _productState =
        MutableLiveData<Resource<ListProductsModels>>(Resource.Idle) //Camba el contenido: como un useState de react (postValue, setValue)
    val productState: LiveData<Resource<ListProductsModels>> get() = _productState //Es al que se observa desde el Fragment

    fun getProducts(
        page: Int? = 1,
        limit: Int? = 16,
        search: String? = null,
        sort: String? = null,
        order: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            //Empieza la carga
            _productState.postValue(Resource.Loading)
            try {
                //petición
                val response = repository.getProducts(page, limit, search, sort, order)
                //retorno de los datos
                if (response != null) {
                    if (response.data.isEmpty()) {
                        val message =
                            if (search?.isNotEmpty() == true) "No se contraron productos con los terminos:\n${search}" else "No se contraron productos"
                        _productState.postValue(Resource.Error(message))
                    } else {
                        _productState.postValue(Resource.Success(response))
                    }
                } else {
                    _productState.postValue(Resource.Error("Error al obtener productos"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _productState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }
}
