package com.example.bytestore.data.repository

import com.example.bytestore.data.model.user.AccountModel
import com.example.bytestore.data.model.user.ListUsersModel
import com.example.bytestore.data.model.user.UserChangeRoleRequest
import com.example.bytestore.data.model.user.UserDeleteRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserProvider
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.model.user.UserUpdateRequest
import com.example.bytestore.data.model.user.toAccountModel
import com.example.bytestore.data.model.user.toUserModel
import com.example.bytestore.data.network.user.UserService
import com.example.bytestore.utils.Resource

class UserRepository {
    private val userService by lazy { UserService() }

    //registro
    suspend fun registerUser(user: UserRegisterRequest): Resource<UserModel> {

        return when(val response = userService.registerUser(user)){
            is Resource.Success -> {
                val account = response.data.toUserModel()
                Resource.Success(account)
            }

            is Resource.Error ->
                Resource.Error(response.message)

            is Resource.ValidationError ->
                Resource.ValidationError(response.errors)

            is Resource.Loading ->
                Resource.Loading

            Resource.Idle ->
                Resource.Idle
        }
    }

    //obtener usuarios
    suspend fun getUsers(page: Int?, search: String?, limit: Int?): Resource<ListUsersModel> {
        val cachedUsers = UserProvider.getFetchUsers()
        //busuqeda local
        if (cachedUsers != null && search != null) {
            val locateSearch = UserProvider.findUser(search)
            if (locateSearch != null) return Resource.Success(locateSearch)
        }
        //Verificar cache en paginación
        if (cachedUsers != null && page != null && (cachedUsers.prev != null && page <= cachedUsers.prev || cachedUsers.next == null)) {
            return Resource.Success(cachedUsers)
        }
        //peticion a la api
        val response = userService.getUsers(page, search, limit)
        if (response is Resource.Success) {
            UserProvider.addFetchUsers(response.data)
        }
        return response
    }

    //obtener usuario por Id
    suspend fun getUser(id: String): Resource<UserModel> {
        //buscar en cache
        val cachedUser = UserProvider.findUserById(id)
        if (cachedUser != null) return Resource.Success(cachedUser)
        val response = userService.getUser(id)
        //almacenar usuario en local
        if (response is Resource.Success) {
            UserProvider.addUser(response.data)
        }
        return response
    }

    //actualizar usuario (admin)
    suspend fun updateUser(
        id: String,
        request: UserUpdateRequest
    ): Resource<UserModel> {
        val response = userService.updateUser(id, request)
        if (response is Resource.Success) {
            UserProvider.addUser(response.data)
        }
        return response
    }

    //cambiar rol
    suspend fun changeRole(id: String, request: UserChangeRoleRequest): Resource<UserModel> {
        val response = userService.changeRole(id, request)
        if (response is Resource.Success) {
            UserProvider.addUser(response.data)
        }
        return response
    }

    //eliminar usuario
    suspend fun deleteUser(
        id: String
    ): Boolean {
        val response = userService.deleteUser(id, null)
        if (response) {
            UserProvider.removeUser(id)
        }
        return response
    }
}