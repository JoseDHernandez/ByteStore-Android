package com.example.bytestore.data.network.User
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body
//Endpoints de la API de usuarios
interface UserApiService {
    @POST("/users/sign-in/")
    suspend fun registerUser(@Body request:UserRegisterRequest):Response<UserModel>
}

