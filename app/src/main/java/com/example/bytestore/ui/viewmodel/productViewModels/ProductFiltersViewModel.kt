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

    //obtener filtros
    fun getProductFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            _productFiltersState.postValue(Resource.Loading)
            try {
                val response = repository.getProductFilters()
                if (response != null) {
                    _productFiltersState.postValue(Resource.Success(response))
                } else {
                    _productFiltersState.postValue(Resource.Error("Error al obtener los filtros de productos"))
                }
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
    }

}