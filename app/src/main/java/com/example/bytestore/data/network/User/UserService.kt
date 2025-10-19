package com.example.bytestore.data.network.User

import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserRegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class UserService {
    private val Api = ApiClient.retrofit()

    suspend fun registerUser(user: UserRegisterRequest): UserModel? = withContext(Dispatchers.IO) {
        try {
            val response: Response<UserModel> =
                Api.create(UserApiService::class.java).registerUser(user)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun loginUser(credentials: UserLoginRequest): UserModel?=withContext(Dispatchers.IO) {
        try {
            val response: Response<UserModel> = Api.create(UserApiService::class.java).loginUser(credentials)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}