package com.example.bytestore.data.repository

import android.content.Context
import com.example.bytestore.data.local.UserPreferences
import com.example.bytestore.data.model.user.AccountModel
import com.example.bytestore.data.model.user.ListUsersModel
import com.example.bytestore.data.model.user.UserChangePasswordRequest
import com.example.bytestore.data.model.user.UserChangeRoleRequest
import com.example.bytestore.data.model.user.UserDeleteRequest
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserProvider
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.model.user.UserUpdateRequest
import com.example.bytestore.data.model.user.toAccountModel
import com.example.bytestore.data.network.user.UserService
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val context: Context) {
    private val userService by lazy { UserService() }
    private val prefs = UserPreferences(context) //datastorage

    //registro
    suspend fun registerUser(user: UserRegisterRequest): Resource<AccountModel> {
        val response = userService.registerUser(user)
        //Almacenar en local
        if(response is Resource.Success) saveUser(response.data)
        return response
    }

    //inicio de sesión
    suspend fun loginUser(credentials: UserLoginRequest): Resource<AccountModel> {
        val response = userService.loginUser(credentials)

        //obtener los datos almacenados y limpiarlos si es otro usuario
        val currentUser = prefs.getUser()
        if (currentUser["email"] != credentials.email) prefs.clearData()
        //almacenar datos
        if(response is Resource.Success) saveUser(response.data)
        return response
    }

    //autenticar por jtw
    suspend fun authJWT(): Boolean {
        val response = userService.authJWT()
        if (response is Resource.Success) {
            saveUser(response.data)
            return true
        }
        return false
    }



    //actualizar cuenta (cuenta local)
    suspend fun updateUser(
        id: String,
        request: UserUpdateRequest
    ): Resource<AccountModel> {
        return when (val response = userService.updateUser(id, request)) {

            is Resource.Success -> {
                val account = response.data.toAccountModel()
                saveUser(account)
                Resource.Success(account)
            }

            is Resource.Error ->
                Resource.Error(response.message)

            is Resource.ValidationError ->
                Resource.ValidationError(response.errors)

            is Resource.Loading ->
                Resource.Loading

            Resource.Idle ->
                Resource.Idle
        }
    }


    //cambiar contraseña
    suspend fun changePassword(id: String, request: UserChangePasswordRequest): Boolean {
        return userService.changePassword(id, request)
    }

    //eliminar usuario
    suspend fun deleteUser(
        id: String,
        request: UserDeleteRequest
    ): Boolean {
        val response = userService.deleteUser(id, request)
        if (response) {
            UserProvider.removeUser(id)
        }
        return response
    }

    //=======================================
    //        Almacenamiento local
    //=======================================

    //almacenar usuario
    private suspend fun saveUser(response: AccountModel) {
        response.let { user ->
            prefs.saveUserData(
                id = user.id,
                name = user.name,
                email = user.email,
                address = user.physicalAddress,
                role = user.role,
                token = user.token
            )
        }
    }

    //obtener token
    suspend fun getUserToken(): String? = prefs.getToken()

    //obtener datos del usuario
    suspend fun getUserData(): AccountModel? {
        val userMap = prefs.getUser()

        val id = userMap["id"]
        val name = userMap["name"]
        val email = userMap["email"]
        val address = userMap["address"]
        val role = userMap["role"]
        val token = userMap["token"]

        return if (id != null && name != null && email != null && address != null && role != null && token != null) {
            AccountModel(
                id = id,
                name = name,
                email = email,
                physicalAddress = address,
                role = role,
                token = token
            )
        } else {
            null
        }
    }

    //cerrar sesión
    suspend fun logout() = prefs.clearData()

    suspend fun isLoggedIn(): Boolean {
        return !getUserToken().isNullOrEmpty()
    }

    //Flows
    val userTokenFlow: Flow<String?> = prefs.userTokenFlow
    val isLoggedInFlow: Flow<Boolean> = prefs.isUserLoggedInFlow

    //obtener rol
    suspend fun getUserRole() = prefs.getUserRole()?.uppercase()
}