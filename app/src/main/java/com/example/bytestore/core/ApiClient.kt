package com.example.bytestore.core

import android.content.Context
import com.example.bytestore.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000"
    private var retrofit: Retrofit? = null

    fun retrofit(context: Context? = null): Retrofit {
        //retornar si ya existe
        if (retrofit != null) return retrofit!!

        //validar contexto
        requireNotNull(context) {
            "ApiClient no ha sido inicializado. Se requiere: ApiClient.retrofit(context) o ApiClient.init(context)."
        }

        val sessionManager = SessionManager(context)
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(HeaderInterceptor(sessionManager))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit!!
    }

    fun init(context: Context) {
        retrofit(context)
    }
}
