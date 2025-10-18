package com.example.bytestore.data.repository

import android.content.Context
import com.example.bytestore.data.local.UserPreferences
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.network.User.UserService

class UserRepository(private val context: Context) {
    private val Api = UserService()
    private val prefs = UserPreferences(context) //datastorage
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

    //obtener token
    suspend fun getUserToken(): String? = prefs.getToken()
    //obtener datos del usuario
    suspend fun getUserData(): Map<String, String?> = prefs.getUser()
    //cerrar sesión
    suspend fun logout() = prefs.clearData()
}