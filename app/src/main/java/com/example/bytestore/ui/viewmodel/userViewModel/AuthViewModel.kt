package com.example.bytestore.ui.viewmodel.userViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.repository.UserRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//datos para las validaciones
data class UserRegisterInput(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val address: String
)

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    //regex
    private val passwordRegex =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$")
    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")


    //livedata (peticiones)
    private val _authState = MutableLiveData<Resource<UserModel>>(Resource.Idle)
    val authState: LiveData<Resource<UserModel>> get() = _authState


    //funcion de registro
    fun registerUser(userRegisterInput: UserRegisterInput) = viewModelScope.launch(Dispatchers.IO) {
        //validaciones
        val errors = mutableMapOf<String, String>()

        //nombre
        val name = userRegisterInput.name.trim()
        val nameRegex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")
        when {
            name.length < 6 -> errors["name"] = "Debe tener al menos 6 caracteres"
            name.length > 200 -> errors["name"] = "No puede exceder 200 caracteres"
            !nameRegex.matches(name) -> errors["name"] = "Solo letras y espacios"
        }

        //correo
        val email = userRegisterInput.email.trim()
        when {
            email.length < 5 -> errors["email"] = "Debe tener al menos 5 caracteres"
            email.length > 300 -> errors["email"] = "No puede exceder 300 caracteres"
            !emailRegex.matches(email) -> errors["email"] = "Formato de correo no válido"
        }

        //contraseña
        val password = userRegisterInput.password.trim()
        when {
            password.length < 8 -> errors["password"] = "Debe tener al menos 8 caracteres"
            password.length > 20 -> errors["password"] = "No debe exceder los 20 caracteres"
            !passwordRegex.matches(password) ->
                errors["password"] =
                    "Debe incluir mayúscula, minúscula, número y carácter especial"
        }

        //contraseña de confirmarción
        val confirmPassword = userRegisterInput.confirmPassword.trim()
        if (confirmPassword != password) {
            errors["confirmPassword"] = "Las contraseñas no coinciden"
        }

        //dirección
        val address = userRegisterInput.address.trim()
        val addressRegex = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\\s\\.,'\"#°\\-]+$")
        when {
            address.length < 2 -> errors["address"] = "La dirección es muy corta"
            address.length > 100 -> errors["address"] = "No debe exceder 100 caracteres"
            !addressRegex.matches(address) -> errors["address"] =
                "Caracteres inválidos en la dirección"
        }

        //retornar errores si existen
        if (errors.isNotEmpty()) {
            _authState.postValue(Resource.ValidationError(errors))
            return@launch
        }

        //paso de userInput a UserRegisterRequest
        val request = UserRegisterRequest(
            name = userRegisterInput.name,
            email = userRegisterInput.email,
            password = userRegisterInput.password,
            physicalAddress = userRegisterInput.address
        )

        val response = repository.registerUser(request)

        _authState.postValue(response)
    }

    //login
    fun loginUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {

        val errors = mutableMapOf<String, String>()

        //correo
        val email = email.trim()
        when {
            email.length < 5 -> errors["email"] = "Debe tener al menos 5 caracteres"
            email.length > 300 -> errors["email"] = "No puede exceder 300 caracteres"
            !emailRegex.matches(email) -> errors["email"] = "Formato de correo no válido"
        }

        //contraseña
        val password = password.trim()
        when {
            password.length < 8 -> errors["password"] = "Debe tener al menos 8 caracteres"
            password.length > 20 -> errors["password"] = "No debe exceder los 20 caracteres"
            !passwordRegex.matches(password) ->
                errors["password"] = "Debe incluir mayúscula, minúscula, número y carácter especial"
        }
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
//TODO: Pendiente de validar usuario almacenado en local

}

