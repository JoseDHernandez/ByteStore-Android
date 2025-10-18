package com.example.bytestore.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bytestore.data.repository.UserRepository

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    //se sobreescribe el viewmodel (modelClass)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //repositorio de usuario
        val userRepository = UserRepository(context)


        //selección del fragment
        return when {

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                RegisterViewModel(UserRepository(context)) as T
            }

            /*
               modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository) as T
            }
            * */
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}