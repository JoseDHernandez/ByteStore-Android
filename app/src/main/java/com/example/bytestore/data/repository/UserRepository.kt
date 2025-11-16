package com.example.bytestore.data.repository

import android.content.Context
import com.example.bytestore.data.local.UserPreferences
import com.example.bytestore.data.model.user.ListUsersModel
import com.example.bytestore.data.model.user.UserChangePasswordRequest
import com.example.bytestore.data.model.user.UserChangeRoleRequest
import com.example.bytestore.data.model.user.UserDeleteRequest
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserProvider
import com.example.bytestore.data.model.user.UserRegisterRequest
import com.example.bytestore.data.model.user.UserUpdateRequest
import com.example.bytestore.data.network.user.UserService
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.flow.Flow

class UserRepository(private val context: Context) {
    private val userService by lazy { UserService() }
    private val prefs = UserPreferences(context) //datastorage

    //registro
    suspend fun registerUser(user: UserRegisterRequest): Resource<UserModel> {
        val response = userService.registerUser(user)
        //Almacenar en local
        saveUser(response)
        return response
    }

    //inicio de sesión
    suspend fun loginUser(credentials: UserLoginRequest): Resource<UserModel> {
        val response = userService.loginUser(credentials)

        //obtener los datos almacenados y limpiarlos si es otro usuario
        val currentUser = prefs.getUser()
        if (currentUser["email"] != credentials.email) prefs.clearData()
        //almacenar datos
        saveUser(response)
        return response
    }

    //autenticar por jtw
    suspend fun authJWT(): Boolean {
        val response = userService.authJWT()
        if (response is Resource.Success) {
            saveUser(response)
            return true
        }
        return false
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

    //actualizar usuario
    suspend fun updateUser(
        id: String,
        request: UserUpdateRequest,
        isAdmin: Boolean = false, //si es administrador
        isLoggedIn: Boolean = true //si el cambio es para la misma cuenta usada
    ): Resource<UserModel> {
        val response = userService.updateUser(id, request)
        //validar si es cliente o administrador
        if (isAdmin && !isLoggedIn) {
            if (response is Resource.Success) {
                UserProvider.addUser(response.data)
            }
        } else {
            saveUser(response)
        }
        return response
    }

    //cambiar contraseña
    suspend fun changePassword(id: String, request: UserChangePasswordRequest): Boolean {
        return userService.changePassword(id, request)
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
    suspend fun deleteUser(id: String, request: UserDeleteRequest,isAdmin: Boolean = false): Boolean {
        val response = userService.deleteUser(id, request)
        if (response) {
            UserProvider.removeUser(id)
        }
        return response
    }

    //=======================================
    //        Almacenamiento local
    //=======================================

    //almacenar usuario
    private suspend fun saveUser(response: Resource<UserModel>) {
        if (response is Resource.Success) {
            response.data.let { user ->
                //validar que los datos no sean nulos
                if (user.id == null || user.token == null) throw IllegalArgumentException("El id o token del usuario no deben ser nulos")
                prefs.saveUserData(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    address = user.physicalAddress,
                    role = user.role,
                    token = user.token
                )
            }
        }
    }

    //obtener token
    suspend fun getUserToken(): String? = prefs.getToken()

    //obtener datos del usuario
    suspend fun getUserData(): UserModel? {
        val userMap = prefs.getUser()

        val id = userMap["id"]
        val name = userMap["name"]
        val email = userMap["email"]
        val address = userMap["address"]
        val role = userMap["role"]
        val token = userMap["token"]

        return if (id != null && name != null && email != null && address != null && role != null && token != null) {
            UserModel(
                id = id,
                name = name,
                email = email,
                physicalAddress = address,
                role = role,
                token = token
            )
        } else {
            null
        }
    }

    //cerrar sesión
    suspend fun logout() = prefs.clearData()

    suspend fun isLoggedIn(): Boolean {
        return !getUserToken().isNullOrEmpty()
    }

    //Flows
    val userTokenFlow: Flow<String?> = prefs.userTokenFlow
    val isLoggedInFlow: Flow<Boolean> = prefs.isUserLoggedInFlow

    //obtener rol
    suspend fun getUserRole() = prefs.getUserRole()?.uppercase()
}