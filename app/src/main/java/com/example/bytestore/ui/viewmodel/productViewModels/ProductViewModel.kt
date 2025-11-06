package com.example.bytestore.ui.viewmodel.productViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.ProductFilters
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.repository.ProductRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    // Estado de productos con Resource
    private val _productsState =
        MutableLiveData<Resource<ListProductsModel>>(Resource.Idle) //Camba el contenido: como un useState de react (postValue, setValue)
    val productsState: LiveData<Resource<ListProductsModel>> get() = _productsState //Es al que se observa desde el Fragment

    //producto
    private val _productState = MutableLiveData<Resource<ProductModel>>(Resource.Idle)
    val productState: LiveData<Resource<ProductModel>> get() = _productState

    //productos similares
    private val _similarProductsState = MutableLiveData<Resource<List<ProductModel>>>(Resource.Idle)
    val similarProductsState: LiveData<Resource<List<ProductModel>>> get() = _similarProductsState



    //obtener todos los productos
    fun getProducts(
        page: Int? = 1,
        limit: Int? = 16,
        search: String? = null,
        sort: String? = null,
        order: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            //Empieza la carga
            _productsState.postValue(Resource.Loading)
            try {
                //petición
                val response = repository.getProducts(page, limit, search, sort, order)
                //retorno de los datos
                if (response != null) {
                    if (response.data.isEmpty()) {
                        val message =
                            if (search?.isNotEmpty() == true) "No se contraron productos con los terminos:\n${search}" else "No se contraron productos"
                        _productsState.postValue(Resource.Error(message))
                    } else {
                        _productsState.postValue(Resource.Success(response))
                    }
                } else {
                    _productsState.postValue(Resource.Error("Error al obtener productos"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _productsState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    //obtener producto por id
    fun getProduct(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _productState.postValue(Resource.Loading)
            try {
                val response = repository.getProduct(id)

                if (response != null) {
                    _productState.postValue(Resource.Success(response))
                } else {
                    _productState.postValue(Resource.Error("Producto no encontrado"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _productsState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    //obtener productos similares según el id de un producto
    fun getSimilarProducts(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _similarProductsState.postValue(Resource.Loading)
            try {
                val response = repository.getSimilarProducts(id)
                if (response != null) {
                    _similarProductsState.postValue(Resource.Success(response))
                } else {
                    _similarProductsState.postValue(Resource.Error("Error al obtener productos similares"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _similarProductsState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

}