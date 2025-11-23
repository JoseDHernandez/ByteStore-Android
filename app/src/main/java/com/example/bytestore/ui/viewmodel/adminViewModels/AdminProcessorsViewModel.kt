package com.example.bytestore.ui.viewmodel.adminViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.product.ProcessorInputs
import com.example.bytestore.data.model.product.ProcessorModel
import com.example.bytestore.data.model.product.ProcessorRegisterRequest
import com.example.bytestore.data.model.product.ProcessorUpdateRequest
import com.example.bytestore.data.model.product.ProductValidator
import com.example.bytestore.data.repository.ProductRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminProcessorsViewModel() : ViewModel() {
    private val repository = ProductRepository()

    //procesadores
    private val _processorsState = MutableLiveData<Resource<List<ProcessorModel>>>(Resource.Idle)
    val processorsState: LiveData<Resource<List<ProcessorModel>>> get() = _processorsState

    //procesador
    private val _processorState = MutableLiveData<Resource<ProcessorModel>>(Resource.Idle)
    val processorState: LiveData<Resource<ProcessorModel>> get() = _processorState

    //procesador de actulizacion
    private val _processorUpdateState = MutableLiveData<Resource<ProcessorModel>>(Resource.Idle)
    val processorUpdateState: LiveData<Resource<ProcessorModel>> get() = _processorUpdateState

    // obtener procesadores
    fun getProcessors() = viewModelScope.launch(Dispatchers.IO) {
        _processorsState.postValue(Resource.Loading)
        _processorsState.postValue(repository.getProcessors())
    }

    //actulizar procesador
    fun updateProcessor(id: Int, data: ProcessorInputs) = viewModelScope.launch(Dispatchers.IO) {
        val errors = ProductValidator.validateProcessor(data)
        if (errors.isNotEmpty()) {
            _processorUpdateState.postValue(Resource.ValidationError(errors))
            return@launch
        }
        val request =
            ProcessorUpdateRequest(data.brand, data.family, data.model, data.cores, data.speed)
        _processorUpdateState.postValue(repository.updateProcessor(id, request))
    }
    //eliminar procesador
    suspend fun deleteProcessor(id:Int)=repository.deleteProcessor(id)
    //registrar procesador
    fun registerProcessor(data: ProcessorInputs)=  viewModelScope.launch(Dispatchers.IO){
        val errors = ProductValidator.validateProcessor(data)
        if (errors.isNotEmpty()) {
            _processorState.postValue(Resource.ValidationError(errors))
            return@launch
        }
        val request = ProcessorRegisterRequest(data.brand, data.family, data.model, data.cores, data.speed)
        _processorState.postValue(repository.registerProcessor(request))
    }
}