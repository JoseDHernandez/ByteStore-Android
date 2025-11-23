package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.data.model.user.UserRegisterInputs
import com.example.bytestore.databinding.FragmentAdminUserRegisterBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.adminViewModels.AdminUsersViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar
import com.google.android.material.snackbar.Snackbar


class AdminUserRegisterFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding: FragmentAdminUserRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminUsersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Registrar usuario")
        //cancelar
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
        //Click en el boton de registro
        binding.buttonSingUp.setOnClickListener {
            //obtener los datos
            val request = UserRegisterInputs(
                name = binding.inputName.text.toString(),
                email = binding.inputEmail.text.toString(),
                password = binding.inputPassword.text.toString(),
                confirmPassword = binding.inputPasswordConfirm.text.toString(),
                address = binding.inputAddress.text.toString()
            )
            viewModel.registerUser(request) //enviar al viewmodel los datos
        }
        //registro
        viewModel.userSate.observe(viewLifecycleOwner) { state ->
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
                    binding.buttonSingUp.text = "Registrar"
                    showValidationErrors(state.errors)
                }
                //Cuando termina
                is Resource.Success -> {
                    binding.buttonSingUp.isEnabled = true
                    binding.buttonSingUp.text = "Registrar"
                    //enviar a la vista de AdminUserFragment
                    val action =
                        AdminUserRegisterFragmentDirections.actionAdminUserRegisterFragmentToAdminUserFragment(
                            state.data.id
                        )
                    findNavController().navigate(action)
                }
                //cuando da error
                is Resource.Error -> {
                    binding.buttonSingUp.isEnabled = true
                    binding.buttonSingUp.text = "Registrar"
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()

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
        binding.scrollView.scrollTo(0, 0)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}