package com.example.bytestore.data.network.user

import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserService {
    private val api = ApiClient.retrofit().create(UserApiService::class.java)

    suspend fun registerUser(user: UserRegisterRequest): Resource<UserModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerUser(user)

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
                    when (response.code()) {
                        409 -> return@withContext Resource.ValidationError(mapOf("email" to "Dirección de correo ya utilizada"))
                        else -> return@withContext Resource.Error("Error ${response.code()}: $errorMessage")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }

        }

    suspend fun loginUser(credentials: UserLoginRequest): Resource<UserModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.loginUser(credentials)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                    //validacion de los codigos de estado
                    when (response.code()) {
                        401 -> return@withContext Resource.ValidationError(mapOf("password" to "Contraseña invalida"))
                        404 -> return@withContext Resource.ValidationError(mapOf("email" to "Usuario no encontrado"))
                        else -> return@withContext Resource.Error("Error ${response.code()}: $errorMessage")
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //autenticación por el JWT
    suspend fun authJWT(): Resource<UserModel> =withContext(Dispatchers.IO){
        try {
            val response = api.authJWT()
            if(response.isSuccessful){
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos.")
                }
            }else{
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                //validacion de los codigos de estado
                when (response.code()) {
                    401 -> return@withContext Resource.ValidationError(mapOf("message" to "No autorizado"))
                    403 -> return@withContext Resource.ValidationError(mapOf("message" to "Rol invalido"))
                    404 -> return@withContext Resource.ValidationError(mapOf("message" to "Usuario no encontrado"))
                    else -> return@withContext Resource.Error("Error ${response.code()}: $errorMessage")
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
}