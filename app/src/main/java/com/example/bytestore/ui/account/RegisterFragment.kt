package com.example.bytestore.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentRegisterBinding
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.userViewModel.AuthViewModel
import com.example.bytestore.ui.viewmodel.userViewModel.UserRegisterInput
import com.example.bytestore.utils.Resource
import com.google.android.material.snackbar.Snackbar


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //saltar registro
        binding.skipSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_productsFragment)
        }
        //Barra de regreso
        binding.topBar.setOnBackClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        //boton de iniciar sesión
        binding.buttonSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        //Click en el boton de registro
        binding.buttonSingUp.setOnClickListener {
            //obtener los datos
            val request = UserRegisterInput(
                name = binding.inputName.text.toString(),
                email = binding.inputEmail.text.toString(),
                password = binding.inputPassword.text.toString(),
                confirmPassword = binding.inputPasswordConfirm.text.toString(),
                address = binding.inputAddress.text.toString()
            )
            viewModel.registerUser(request) //enviar al viewmodel los datos
        }
        //registro
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                //Cuando inicia
                is Resource.Idle -> Unit
                //Cuando carga
                is Resource.Loading -> {
                    binding.buttonSingUp.isEnabled = false
                    binding.buttonSingUp.text = "Registrando..."
                    clearErrors()
                }
                //Errores de validación
                is Resource.ValidationError -> {
                    binding.buttonSingUp.isEnabled = true
                    binding.buttonSingUp.text = "Registrarse"
                    showValidationErrors(state.errors)
                }
                //Cuando termina
                is Resource.Success -> {
                    binding.buttonSingUp.isEnabled = true
                    binding.buttonSingUp.text = "Registrarse"
                    //datos
                    val user = state.data
                    Snackbar.make(
                        binding.root,
                        "Usuario registrado correctamente",
                        Snackbar.LENGTH_LONG
                    ).show()
                    //TODO: pendiente almacenar datos en local y token
                    println("Usuario registrado: ${user.name}")
                }
                //cuando da error
                is Resource.Error -> {
                    binding.buttonSingUp.isEnabled = true
                    binding.buttonSingUp.text = "Registrarse"
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    //mostrar error
                }
            }
        }
    }

    //limpiar errores
    private fun clearErrors() {
        binding.inputNameMessage.text = ""
        binding.inputEmailMessage.text = ""
        binding.inputPasswordMessage.text = ""
        binding.inputPasswordConfirmMessage.text = ""
        binding.inputAddressMessage.text = ""
    }

    //validaciones
    private fun showValidationErrors(errors: Map<String, String>) {
        clearErrors()
        errors["name"]?.let { binding.inputNameMessage.text = it }
        errors["email"]?.let { binding.inputEmailMessage.text = it }
        errors["password"]?.let { binding.inputPasswordMessage.text = it }
        errors["confirmPassword"]?.let { binding.inputPasswordConfirmMessage.text = it }
        errors["address"]?.let { binding.inputAddressMessage.text = it }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}