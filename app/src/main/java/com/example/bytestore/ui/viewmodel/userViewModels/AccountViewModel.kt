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
import com.example.bytestore.data.repository.UserRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: UserRepository) : ViewModel() {
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
        val errors = mutableMapOf<String, String>()

        // Validar nombre
        val name = data.name?.trim()
        if (name.isNullOrBlank()) {
            errors["name"] = "El nombre es requerido"
        } else {
            when {
                name.length < 3 -> errors["name"] = "El nombre debe tener al menos 3 caracteres"
                name.length > 200 -> errors["name"] = "El nombre no puede exceder 200 caracteres"
            }
        }

        // Validar email
        val email = data.email?.trim()
        if (email.isNullOrBlank()) {
            errors["email"] = "El correo es requerido"
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errors["email"] = "Formato de correo electrónico inválido"
            }
        }

        // Validar dirección (opcional pero si se envía debe ser válida)
        val address = data.address?.trim()
        if (!address.isNullOrBlank() && address.length < 2) {
            errors["address"] = "La dirección es muy corta"
        }

        // Retornar errores si existen
        if (errors.isNotEmpty()) {
            _userData.postValue(Resource.ValidationError(errors))
            return@launch
        }

        val request = UserUpdateRequest(name, email, address)

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
