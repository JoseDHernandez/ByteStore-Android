package com.example.bytestore.data.network.user

import com.example.bytestore.data.model.user.ListUsersModel
import com.example.bytestore.data.model.user.UserChangePasswordRequest
import com.example.bytestore.data.model.user.UserChangeRoleRequest
import com.example.bytestore.data.model.user.UserDeleteRequest
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.model.user.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

//Endpoints de la API de usuarios
interface UserApiService {
    //Registro
    @POST("/users/sign-up/")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<UserModel>

    //Login
    @POST("/users/sign-in/")
    suspend fun loginUser(@Body request: UserLoginRequest): Response<UserModel>

    //atuh
    @POST("/users/auth/")
    suspend fun authJWT(): Response<UserModel>

    //obtener usuario por id
    @GET("/users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserModel>

    //obtener usuarios (admin)
    @GET("/users")
    suspend fun getUsers(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("search") search: String?
    ): Response<ListUsersModel>

    //actualizar usuario
    @PUT("/users/{id}/")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UserUpdateRequest
    ): Response<UserModel>

    //cambiar constraseña
    @PATCH("/users/{id}/password/")
    suspend fun changePassword(
        @Path("id") id: String,
        @Body request: UserChangePasswordRequest
    ): Response<Any> //solo necesito el status code

    //cambiar rol
    @PATCH("/users/{id}/role/")
    suspend fun changeRole(
        @Path("id") id: String,
        @Body request: UserChangeRoleRequest
    ): Response<UserModel>

    //Eliminar cuenta (si es cliente necesita enviar la contraseña)
    @DELETE("/users/{id}/")
    suspend fun deleteUser(
        @Path("id") id: String,
        @Body request: UserDeleteRequest
    ): Response<Any> //validar status code
}

