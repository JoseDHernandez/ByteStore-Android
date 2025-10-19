package com.example.bytestore.data.repository

import android.content.Context
import com.example.bytestore.data.local.UserPreferences
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.network.User.UserService

class UserRepository(private val context: Context) {
    private val Api = UserService()
    private val prefs = UserPreferences(context) //datastorage

    //registro
    suspend fun registerUser(user: UserRegisterRequest): UserModel? {
        val response: UserModel? = Api.registerUser(user)
        //Almacenar en local
        response?.let { user ->
            prefs.saveUserData(
                id = user.id,
                name = user.name,
                email = user.email,
                address = user.physicalAddress,
                role = user.role,
                token = user.token
            )
        }
        return response
    }

    //inicio de sesión
    suspend fun loginUser(credentials: UserLoginRequest): UserModel? {
        val response: UserModel? = Api.loginUser(credentials)
        //obtener los datos almacenados y limpiarlos si es otro usuario
        val currentUser = prefs.getUser()
        if (currentUser["email"] != credentials.email) prefs.clearData()
        //almacenar datos
        response?.let { user ->
            prefs.saveUserData(
                id = user.id,
                name = user.name,
                email = user.email,
                address = user.physicalAddress,
                role = user.role,
                token = user.token
            )
        }
        return response
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
}