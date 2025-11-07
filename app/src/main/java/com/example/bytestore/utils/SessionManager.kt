package com.example.bytestore.utils

import android.content.Context
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class SessionManager(private val context: Context) {
    private val repository = UserRepository(context)

    @Volatile
    private var cachedToken: String? = null

    init {
        // Cache del token en memoria
        CoroutineScope(Dispatchers.IO).launch {
            userTokenFlow.collect { token ->
                cachedToken = token
            }
        }
    }

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
    suspend fun getCurrentUser(): UserModel? = repository.getUserData()

    //obtener token
    suspend fun getToken(): String? {
        return cachedToken ?: repository.getUserToken().also {
            cachedToken = it
        }
    }
    //validar estado de la sesión
    val userTokenFlow: Flow<String?> = repository.userTokenFlow
    val isLoggedInFlow: Flow<Boolean> = repository.isLoggedInFlow

}