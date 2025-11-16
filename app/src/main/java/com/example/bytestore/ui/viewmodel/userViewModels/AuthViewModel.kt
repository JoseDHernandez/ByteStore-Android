package com.example.bytestore.ui.viewmodel.userViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.user.AccountModel
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterInputs
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.model.user.UserValidator
import com.example.bytestore.data.repository.UserRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    //livedata (peticiones)
    private val _authState = MutableLiveData<Resource<AccountModel>>(Resource.Idle)
    val authState: LiveData<Resource<AccountModel>> get() = _authState


    //funcion de registro
    fun registerUser(userRegisterInputs: UserRegisterInputs) = viewModelScope.launch(Dispatchers.IO) {
        //validaciones
        val errors = UserValidator.validateRegister(userRegisterInputs)

        //retornar errores si existen
        if (errors.isNotEmpty()) {
            _authState.postValue(Resource.ValidationError(errors))
            return@launch
        }

        //paso de userInput a UserRegisterRequest
        val request = UserRegisterRequest(
            name = userRegisterInputs.name,
            email = userRegisterInputs.email,
            password = userRegisterInputs.password,
            physicalAddress = userRegisterInputs.address
        )

        val response = repository.registerUser(request)

        _authState.postValue(response)
    }

    //login
    fun loginUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {

        val errors = UserValidator.validateLogin(email,password)

        if (errors.isNotEmpty()) {
            _authState.postValue(Resource.ValidationError(errors))
            return@launch
        }
        //crear request
        val request = UserLoginRequest(email, password)
        //petición
        val response = repository.loginUser(request)
        _authState.postValue(response)
    }

}

