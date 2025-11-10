package com.example.bytestore.data.model.user

import com.google.gson.annotations.SerializedName

data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("physical_address") val physicalAddress: String,
    val role: String,
    val token: String
)

//Formato de petición de login
data class UserLoginRequest(
    val email: String,
    val password: String
)