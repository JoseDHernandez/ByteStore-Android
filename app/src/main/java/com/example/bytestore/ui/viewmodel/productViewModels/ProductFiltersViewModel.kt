package com.example.bytestore.ui.viewmodel.productViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.product.ProductFilters
import com.example.bytestore.data.repository.ProductRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductFiltersViewModel : ViewModel() {
    private val repository = ProductRepository()

    //filtros
    private val _productFiltersState = MutableLiveData<Resource<ProductFilters>>(Resource.Idle)
    val productFiltersState: LiveData<Resource<ProductFilters>> get() = _productFiltersState

    //Datos de los filtros
    private val _selectedBrands = MutableLiveData<Set<String>>(emptySet())
    val selectedBrands: LiveData<Set<String>> = _selectedBrands

    private val _selectedProcessors = MutableLiveData<Set<String>>(emptySet())
    val selectedProcessors: LiveData<Set<String>> = _selectedProcessors

    private val _selectedDisplays = MutableLiveData<Set<String>>(emptySet())
    val selectedDisplays: LiveData<Set<String>> = _selectedDisplays

    private val _selectedOrder = MutableLiveData<String>("Ordernar por")
    val selectedOrder: LiveData<String> = _selectedOrder
    //Opciones de filtros
    val orderOptions = listOf(
        "Ordernar por",
        "Relevancia",
        "Precio: menor a mayor",
        "Precio: mayor a menor"
    )
    //obtener filtros
    fun getProductFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            _productFiltersState.postValue(Resource.Loading)
            try {
                val response = repository.getProductFilters()
                _productFiltersState.postValue(Resource.Success(response))
            } catch (e: Exception) {
                e.printStackTrace()
                _productFiltersState.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    //cambiar seleccion de marca
    fun toggleBrandSelection(brand: String) {
        val updated = _selectedBrands.value!!.toMutableSet().apply {
            if (contains(brand)) remove(brand) else add(brand)
        }
        _selectedBrands.value = updated
    }

    //cambiar seleccion de procesador
    fun toggleProcessorSelection(processor: String) {
        val updated = _selectedProcessors.value!!.toMutableSet().apply {
            if (contains(processor)) remove(processor) else add(processor)
        }
        _selectedProcessors.value = updated
    }

    //cambiar seleccion de tarjeta grafica
    fun toggleDisplaySelection(display: String) {
        val updated = _selectedDisplays.value!!.toMutableSet().apply {
            if (contains(display)) remove(display) else add(display)
        }
        _selectedDisplays.value = updated
    }

    //limpiar selección
    fun clearSelections() {
        _selectedBrands.value = emptySet()
        _selectedProcessors.value = emptySet()
        _selectedDisplays.value = emptySet()
        _selectedOrder.value= "Ordernar por"
    }

    //seccion del orden
    fun setSelectedOrder(order: String) {
        _selectedOrder.value = order
    }

    //pasar selección (string) a los parametros de la API
    fun getSelectedOrder(): Map<String, String> {
       val order = _selectedOrder.value
        if (!order.isNullOrEmpty() && orderOptions.contains(order) && orderOptions.indexOf(order) != 0) {
            val sortType = if (order == "Relevancia") "order_review" else "order_price"
            val orderType = if (order.contains("menor a mayor")) "ASC" else "DESC"
            return mapOf(
                "sort" to sortType,
                "order" to orderType
            )
        }
        return emptyMap()
    }
}