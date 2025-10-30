package com.example.bytestore.core

import com.example.bytestore.utils.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //obtener token
        val token = runBlocking {
            if (sessionManager.isLoggedIn()) sessionManager.getToken() else null
        }
        //añadir cabecera a la petición
        val request = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            request.addHeader("Authorization", token)
        }
        return chain.proceed(request.build())
    }

}