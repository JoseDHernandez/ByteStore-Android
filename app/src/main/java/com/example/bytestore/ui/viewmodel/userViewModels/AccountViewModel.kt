package com.example.bytestore.ui.viewmodel.userViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.user.AccountModel
import com.example.bytestore.data.model.user.UserChangePasswordRequest
import com.example.bytestore.data.model.user.UserDeleteRequest
import com.example.bytestore.data.model.user.UserUpdateInputs
import com.example.bytestore.data.model.user.UserUpdateRequest
import com.example.bytestore.data.model.user.UserValidator
import com.example.bytestore.data.repository.AccountRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {
    //livedata (usuario)
    private val _userData = MutableLiveData<Resource<AccountModel?>>()
    val userData: LiveData<Resource<AccountModel?>> get() = _userData

    private val _logoutState = MutableLiveData<Resource<Unit>>(Resource.Idle)
    val logoutState: LiveData<Resource<Unit>> get() = _logoutState

    init {
        getUserData()
    }

    //datos de la cuenta
    fun getUserData() = viewModelScope.launch {
        val data = repository.getUserData()
        _userData.postValue(Resource.Success(data))
    }

    //cerrar sesión
    fun logout() = viewModelScope.launch {
        try {
            repository.logout()
            _logoutState.postValue(Resource.Success(Unit))
            _userData.postValue(Resource.Idle)
        } catch (e: Exception) {
            _logoutState.postValue(Resource.Error("Error al cerrar sesión: ${e.message}"))
        }
    }

    //actualizar cuenta
    fun updateAccount(data: UserUpdateInputs) = viewModelScope.launch(Dispatchers.IO) {

        // Validar todos los campos (el backend requiere todos)
        val errors = UserValidator.validateUpdateUser(data)

        // Retornar errores si existen
        if (errors.isNotEmpty()) {
            _userData.postValue(Resource.ValidationError(errors))
            return@launch
        }

        val request = UserUpdateRequest(data.name, data.email, data.address)

        try {
            val id = repository.getUserData()?.id ?: run {
                _userData.postValue(Resource.Error("No se pudo obtener el ID del usuario"))
                return@launch
            }

            val response = repository.updateUser(id, request)
            _userData.postValue(response)
        } catch (e: Exception) {
            _userData.postValue(Resource.Error("Error al actualizar los datos de la cuenta: ${e.message}"))
        }
    }

    //cambiar contraseña
    fun changePassword(request: UserChangePasswordRequest) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val id = repository.getUserData()?.id ?: return@launch
            val response = repository.changePassword(id, request)
            //cerrar session si cambió la contraseña
            if (response) {
                logout()
            } else {
                _userData.postValue(Resource.Error("Error al cambiar la contraseña"))
            }
        } catch (e: Exception) {
            _userData.postValue(Resource.Error("Error al cambiar la contraseña"))
        }
    }

    //eliminar cuenta
    fun deleteAccount(password: String?) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val role = repository.getUserRole() == "ADMINISTRADOR"
            val id = repository.getUserData()?.id ?: return@launch
            if (!role && !UserValidator.passwordRegex().matches(password?.trim() ?: "")) {
                _userData.postValue(Resource.ValidationError(mapOf("password" to "Contraseña invalida")))
            } else {
                val response = repository.deleteUser(id, UserDeleteRequest(password))
                //cerrar session si se eliminó la cuenta
                if (response) {
                    logout()
                } else {
                    _userData.postValue(Resource.Error("Error al eliminar la contraseña"))
                }
            }
        } catch (e: Exception) {
            _userData.postValue(Resource.Error("Error al eliminar la cuenta"))
        }
    }
}
