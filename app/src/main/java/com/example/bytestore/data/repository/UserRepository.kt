package com.example.bytestore.data.repository

import android.content.Context
import com.example.bytestore.data.local.UserPreferences
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.network.user.UserService
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.flow.Flow

class UserRepository(private val context: Context) {
    private val userService by lazy { UserService() }
    private val prefs = UserPreferences(context) //datastorage

    //registro
    suspend fun registerUser(user: UserRegisterRequest): Resource<UserModel> {
        val response = userService.registerUser(user)
        //Almacenar en local
        saveUser(response)
        return response
    }

    //inicio de sesión
    suspend fun loginUser(credentials: UserLoginRequest): Resource<UserModel> {
        val response = userService.loginUser(credentials)

        //obtener los datos almacenados y limpiarlos si es otro usuario
        val currentUser = prefs.getUser()
        if (currentUser["email"] != credentials.email) prefs.clearData()
        //almacenar datos
        saveUser(response)
        return response
    }

    //almacenar usuario
    private suspend fun saveUser(response: Resource<UserModel>) {
        if (response is Resource.Success) {
            response.data.let { user ->
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
    }

    //obtener token
    suspend fun getUserToken(): String? = prefs.getToken()

    //obtener datos del usuario
    suspend fun getUserData(): UserModel? {
        val userMap = prefs.getUser()

        val id = userMap["id"]
        val name = userMap["name"]
        val email = userMap["email"]
        val address = userMap["address"]
        val role = userMap["role"]
        val token = userMap["token"]

        return if (id != null && name != null && email != null && address != null && role != null && token != null) {
            UserModel(
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
    suspend fun getUserRole() = prefs.getUserRole()
}