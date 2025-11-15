package com.example.bytestore.data.network.user

import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.user.ListUsersModel
import com.example.bytestore.data.model.user.UserChangePasswordRequest
import com.example.bytestore.data.model.user.UserChangeRoleRequest
import com.example.bytestore.data.model.user.UserDeleteRequest
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.model.user.UserUpdateRequest
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
    suspend fun authJWT(): Resource<UserModel> = withContext(Dispatchers.IO) {
        try {
            val response = api.authJWT()
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
                    401 -> return@withContext Resource.ValidationError(mapOf("message" to "No autorizado"))
                    403 -> return@withContext Resource.ValidationError(mapOf("message" to "Rol invalido"))
                    404 -> return@withContext Resource.ValidationError(mapOf("message" to "Usuario no encontrado"))
                    else -> return@withContext Resource.Error("Error ${response.code()}: $errorMessage")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    // Obtener usuario por id (admin)
    suspend fun getUser(id: String): Resource<UserModel> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUser(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos.")
                }
            } else {
                Resource.Error("No se encontro el usuario con el id: $id")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //obtener usuarios (admin)
    suspend fun getUsers(page: Int?, search: String?, limit: Int?): Resource<ListUsersModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUsers(page, limit, search)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    Resource.Error("Error al obtener usuarios")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //actualizar usuario (cliente y admin)
    //si el usuario es cliente el request es obligatorio
    suspend fun updateUser(id: String, request: UserUpdateRequest): Resource<UserModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateUser(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    Resource.Error("Error al actulizar el usuario con el id: $id")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //cambiar contraseña
    suspend fun changePassword(id: String, request: UserChangePasswordRequest): Boolean =
        withContext(
            Dispatchers.IO
        ) {
            try {
                val response = api.changePassword(id, request)
                //retornar verdadero si se cambio la contraseña (200), de lo contrario false
                response.code() == 200;
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    //Cambiar rol (admin)
    suspend fun changeRole(id: String, request: UserChangeRoleRequest): Resource<UserModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.changeRole(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    Resource.Error("Error al cambiar el rol (${request.role}) del usuario con el id: $id")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //eliminar usuario (admin)
    suspend fun deleteUser(id: String, request: UserDeleteRequest): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response = api.deleteUser(id, request)
                //retornar verdadero si se elimino a el usuario (200), de lo contrario false
                response.code() == 200;
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}