package com.example.bytestore.utils

import android.content.Context
import com.example.bytestore.data.model.user.AccountModel
import com.example.bytestore.data.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class SessionManager(private val context: Context) {
    private val repository = AccountRepository(context)

    @Volatile
    private var cachedToken: String? = null
    //validar estado de la sesión
    val userTokenFlow: Flow<String?> = repository.userTokenFlow
    val isLoggedInFlow: Flow<Boolean> = repository.isLoggedInFlow
    init {
        startObservingToken()
    }
    // Observador del token
    private fun startObservingToken() {
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
    suspend fun getCurrentUser(): AccountModel? = repository.getUserData()

    //obtener token
    suspend fun getToken(): String? {
        return cachedToken ?: repository.getUserToken().also {
            cachedToken = it
        }
    }

    //autenticar por token
    suspend fun authToken(): Boolean {
        if(getToken()!=null){
            return repository.authJWT()
        }
        return false
    }
    //cerrar sesión
    suspend fun logout() =repository.logout()
}