package com.example.bytestore.utils

import android.content.Context
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.repository.UserRepository


class SessionManager(private val context: Context) {
    private val repository = UserRepository(context)

    //validar el acceso de la cuenta
    suspend fun checkAccess(requiredRole: String? = null): Boolean {
        val token = repository.getUserToken()
        val user = repository.getUserData()
        //validar si tiene token = esta logeado
        if (token.isNullOrEmpty()) return false
        //validar rol
        if (requiredRole != null && user?.role != requiredRole.uppercase()) return false
        return true
    }

    //obtener usuario
    suspend fun getCurrentUser(): UserModel? {
        return repository.getUserData()
    }

    //cerrar sesion
    suspend fun logout(): Boolean {
        return try {
            repository.logout()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    //obtener token
    suspend fun getToken(): String? {
        return repository.getUserToken()
    }
    //validar estado de la sesión
    suspend fun isLoggedIn(): Boolean {
        return !repository.getUserToken().isNullOrEmpty()
    }
}