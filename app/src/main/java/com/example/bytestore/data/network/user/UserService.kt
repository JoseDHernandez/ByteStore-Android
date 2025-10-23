package com.example.bytestore.data.network.user

import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserService {
    private val Api = ApiClient.retrofit()

    suspend fun registerUser(user: UserRegisterRequest): Resource<UserModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = Api.create(UserApiService::class.java).registerUser(user)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                        //TODO: Pendiente validacion de los codigos de estado
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                    Resource.Error("Error ${response.code()}: $errorMessage")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }

        }

    suspend fun loginUser(credentials: UserLoginRequest): Resource<UserModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = Api.create(UserApiService::class.java).loginUser(credentials)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    //mensajes de error
                    val errors = mutableMapOf<String, String>()
                    val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                    //validacion de los codigos de estado
                    val responseCode = response.code()
                    if (responseCode == 401) {
                        errors["password"] = "Contraseña invalida"
                    }
                    if (responseCode == 404) {
                        errors["email"] = "Usuario no encontrado"
                    }
                    if (errors.isNotEmpty()) {
                        Resource.ValidationError(errors)
                    }
                    Resource.Error("Error ${response.code()}: $errorMessage")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
}