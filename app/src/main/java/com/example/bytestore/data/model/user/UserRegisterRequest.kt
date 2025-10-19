package com.example.bytestore.data.model.user

import com.google.gson.annotations.SerializedName
//TODO: Pasar a UserModel
//datos de la petición de registro
data class UserRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("physical_address") val physicalAddress: String
)
