package com.example.bytestore.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bytestore.data.repository.UserRepository
import com.example.bytestore.ui.viewmodel.userViewModels.AccountViewModel
import com.example.bytestore.ui.viewmodel.userViewModels.AuthViewModel

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    //se sobreescribe el viewmodel (modelClass)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //repositorio de usuario
        val userRepository = UserRepository(context)


        //selección del fragment
        return when {
            //viewmodel de auth: (registro, login)
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(userRepository) as T
            }
            //viewmodel de account (logout, getdata, updateuser)
            modelClass.isAssignableFrom(AccountViewModel::class.java) -> {
                AccountViewModel(userRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}