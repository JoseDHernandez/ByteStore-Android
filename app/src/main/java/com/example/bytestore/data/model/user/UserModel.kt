package com.example.bytestore.data.model.user

import com.google.gson.annotations.SerializedName

data class AccountModel(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("physical_address") val physicalAddress: String,
    val role: String,
    val token: String
)

data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("physical_address") val physicalAddress: String,
    val role: String,
    val token: String?
)

//conversor de Usermodel a AccountModel
fun UserModel.toAccountModel(): AccountModel {
    return AccountModel(
        id = this.id,
        name = this.name,
        email = this.email,
        physicalAddress = this.physicalAddress,
        role = this.role,
        token = this.token ?: ""
    )
}

//conversor de AccountModel a Usermodel
fun AccountModel.toUserModel(): UserModel {
    return UserModel(
        id = this.id,
        name = this.name,
        email = this.email,
        physicalAddress = this.physicalAddress,
        role = this.role,
        token = this.token
    )
}


//=============================================
//             Lista de usuarios
//=============================================

data class ListUsersModel(
    val total: Int,
    val pages: Int,
    val first: Int,
    val next: Int? = null,
    val prev: Int? = null,
    val data: List<UserModel> = emptyList()
)

//=============================================
//             Login
//=============================================

data class UserLoginRequest(
    val email: String,
    val password: String
)

//=============================================
//             Cambiar contraseña
//=============================================

data class UserChangePasswordRequest(
    val password: String
)

//=============================================
//             Datos de registro
//=============================================

data class UserRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("physical_address") val physicalAddress: String
)

//=============================================
//             Datos de actulización
//=============================================

data class UserUpdateRequest(
    val name: String?,
    val email: String?,
    @SerializedName("physical_address") val physicalAddress: String?
)

//=============================================
//             Datos de cambiar rol
//=============================================

data class UserChangeRoleRequest(
    val role: String
)

//=============================================
//             Eliminar cuenta (cliente)
//=============================================

data class UserDeleteRequest(
    val password: String?
)