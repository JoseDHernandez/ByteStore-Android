package com.example.bytestore.ui.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.repository.UserRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//datos para las validaciones
data class UserInput(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val address: String? = null
)

class RegisterViewModel (private val repository: UserRepository) : ViewModel() {

    //livedata
    private val _registerState = MutableLiveData<Resource<UserModel>>(Resource.Idle)
    val registerState: LiveData<Resource<UserModel>> get() = _registerState
    fun registerUser(userInput: UserInput) = viewModelScope.launch(Dispatchers.IO) {
        //validaciones
        val errors = mutableMapOf<String, String>()

        //nombre
        val name = userInput.name?.trim()
        if (name.isNullOrEmpty()) {
            errors["name"] = "El nombre es obligatorio"
        } else {
            val nameRegex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")
            when {
                name.length < 6 -> errors["name"] = "Debe tener al menos 6 caracteres"
                name.length > 200 -> errors["name"] = "No puede exceder 200 caracteres"
                !nameRegex.matches(name) -> errors["name"] = "Solo letras y espacios"
            }
        }

        //correo
        val email = userInput.email?.trim()
        if (email.isNullOrEmpty()) {
            errors["email"] = "El correo es obligatorio"
        } else {
            val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
            when {
                email.length < 5 -> errors["email"] = "Debe tener al menos 5 caracteres"
                email.length > 300 -> errors["email"] = "No puede exceder 300 caracteres"
                !emailRegex.matches(email) -> errors["email"] = "Formato de correo no válido"
            }
        }

        //contraseña
        val password = userInput.password?.trim()
        if (password.isNullOrEmpty()) {
            errors["password"] = "La contraseña es obligatoria"
        } else {
            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$")
            when {
                password.length < 8 -> errors["password"] = "Debe tener al menos 8 caracteres"
                password.length > 20 -> errors["password"] = "No debe exceder los 20 caracteres"
                !passwordRegex.matches(password) ->
                    errors["password"] = "Debe incluir mayúscula, minúscula, número y carácter especial"
            }
        }

        //contraseña de confirmarción
        val confirmPassword = userInput.confirmPassword?.trim()
        if (confirmPassword.isNullOrEmpty()) {
            errors["confirmPassword"] = "Debe confirmar la contraseña"
        } else if (password != null && confirmPassword != password) {
            errors["confirmPassword"] = "Las contraseñas no coinciden"
        }

        //dirección
        val address = userInput.address?.trim()
        if (address.isNullOrEmpty()) {
            errors["address"] = "La dirección es obligatoria"
        } else {
            val addressRegex = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\\s\\.,'\"#°\\-]+$")
            when {
                address.length < 2 -> errors["address"] = "La dirección es muy corta"
                address.length > 100 -> errors["address"] = "No debe exceder 100 caracteres"
                !addressRegex.matches(address) -> errors["address"] = "Caracteres inválidos en la dirección"
            }
        }

        //retornar errores si existen
        if(errors.isNotEmpty()){
            _registerState.postValue(Resource.ValidationError(errors))
            return@launch
        }

        try {
            //paso de userInput a UserRegisterRequest
            val request = UserRegisterRequest(
                name = userInput.name!!,
                email = userInput.email!!,
                password = userInput.password!!,
                physicalAddress = userInput.address!!
            )

            val response = repository.registerUser(request)
            if (response != null) {
                _registerState.postValue(Resource.Success(response))
            } else {
                _registerState.postValue(Resource.Error("No se pudo registrar el usuario"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _registerState.postValue(Resource.Error("Error: ${e.message}"))
        }
    }
}

