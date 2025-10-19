package com.example.bytestore.core

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000"

    //log de peticiones
    private val loggin = HttpLoggingInterceptor().apply {
        level= HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder().addInterceptor(loggin).build()
    //intancia de retrofit
    fun retrofit(): Retrofit {
     return   Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(
         GsonConverterFactory.create()).build()
    }

}