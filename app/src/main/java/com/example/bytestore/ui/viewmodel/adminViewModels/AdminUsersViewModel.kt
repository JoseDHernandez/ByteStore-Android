package com.example.bytestore.ui.viewmodel.adminViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.user.ListUsersModel
import com.example.bytestore.data.model.user.UserChangeRoleRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterInputs
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.model.user.UserUpdateInputs
import com.example.bytestore.data.model.user.UserUpdateRequest
import com.example.bytestore.data.model.user.UserValidator
import com.example.bytestore.data.repository.UserRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminUsersViewModel() : ViewModel() {
    private val repository = UserRepository()

    //usuarios
    private val _usersState = MutableLiveData<Resource<ListUsersModel>>(Resource.Idle)
    val usersState: LiveData<Resource<ListUsersModel>> get() = _usersState

    //usuario
    private val _userState = MutableLiveData<Resource<UserModel>>(Resource.Idle)
    val userSate: LiveData<Resource<UserModel>> get() = _userState

    //actualizar usuario
    private val _userUpdateState = MutableLiveData<Resource<UserModel>>(Resource.Idle)
    val userUpdateState: LiveData<Resource<UserModel>> get() = _userUpdateState

    //obtener usuarios
    fun getUsers(
        page: Int? = 1,
        search: String? = null
    ) = viewModelScope.launch(Dispatchers.IO) {
        _usersState.postValue(Resource.Loading)
        //validar buqueda
        val query = if (search != null) {
            val q = search.trim()
            val searchRegex = Regex("^[0-9A-Za-zÁÉÍÓÚáéíóúÑñ,]{2,300}$")
            if (searchRegex.matches(q)) q else null
        } else null
        //peticion
        val response = repository.getUsers(page, query, 16)
        _usersState.postValue(response)


    }

    //obtener usuario
    fun getUser(id: String) = viewModelScope.launch(Dispatchers.IO) {
        _userState.postValue(Resource.Loading)
        _userState.postValue(repository.getUser(id))
    }

    //actulizar usuario
    fun updateUser(id: String, data: UserUpdateInputs) = viewModelScope.launch(Dispatchers.IO) {
        val errors = UserValidator.validateUpdateUser(data)
        if (errors.isNotEmpty()) {
            _userUpdateState.postValue(Resource.ValidationError(errors))
            return@launch
        }
        //peticion
        val request = UserUpdateRequest(data.name, data.email, data.address)
        _userUpdateState.postValue(repository.updateUser(id, request))
    }

    //eliminar usuario
    suspend fun deleteUser(id: String) = repository.deleteUser(id)

    //cambiar rol
    fun changeRol(id: String, role: String) = viewModelScope.launch(Dispatchers.IO) {
        if (!role.contentEquals("ADMISNISTRADOR") || !role.contentEquals("CLIENTE")) {
            _userState.postValue(Resource.ValidationError(mapOf("rol" to "Rol invalido")))
        }
        _userState.postValue(repository.changeRole(id, UserChangeRoleRequest(role)))
    }

    //registrar usuario
    fun registerUser(userRegisterInputs: UserRegisterInputs) =
        viewModelScope.launch(Dispatchers.IO) {
            val errors = UserValidator.validateRegister(userRegisterInputs)

            //retornar errores si existen
            if (errors.isNotEmpty()) {
                _userState.postValue(Resource.ValidationError(errors))
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

            _userState.postValue(response)
        }
}