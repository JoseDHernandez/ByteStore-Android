package com.example.bytestore.data.network.user
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

//Endpoints de la API de usuarios
interface UserApiService {
    //Registro
    @POST("/users/sign-up/")
    suspend fun registerUser(@Body request:UserRegisterRequest):Response<UserModel>
    //Login
    @POST("/users/sign-in/")
    suspend fun loginUser(@Body request: UserLoginRequest): Response<UserModel>
    //atuh
    @POST("/users/auth/")
    suspend fun authJWT(): Response<UserModel>
}

