package com.example.bytestore.ui.viewmodel.userViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.repository.UserRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: UserRepository) : ViewModel() {
    //livedata (usuario)
    private val _userData = MutableLiveData<UserModel?>()
    val userData: LiveData<UserModel?> get() = _userData

    private val _logoutState = MutableLiveData<Resource<Unit>>(Resource.Idle)
    val logoutState: LiveData<Resource<Unit>> get() = _logoutState

    init {
        getUserData()
    }

    //datos de la cuenta
    fun getUserData() = viewModelScope.launch {
        val data = repository.getUserData()
        _userData.postValue(data)
    }

    //cerrar sesión
    fun logout() = viewModelScope.launch {
        try {
            repository.logout()
            _logoutState.postValue(Resource.Success(Unit))
            _userData.postValue(null)
        } catch (e: Exception) {
            _logoutState.postValue(Resource.Error("Error al cerrar sesión: ${e.message}"))
        }
    }
    //TODO: Pendiente funcion de actualizar cuenta (no opcion de administrador)
}