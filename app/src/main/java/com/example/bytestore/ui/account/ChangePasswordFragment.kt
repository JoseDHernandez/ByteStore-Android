package com.example.bytestore.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.data.model.user.UserChangePasswordRequest
import com.example.bytestore.data.model.user.UserValidator
import com.example.bytestore.databinding.FragmentChangePasswordBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.userViewModels.AccountViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar

class ChangePasswordFragment : ProtectedFragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    private var isChangingPassword = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Cambiar contraseña")

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnChangePassword.setOnClickListener {
            clearErrors()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validatePasswords(newPassword, confirmPassword)) {
                isChangingPassword = true
                binding.btnChangePassword.isEnabled = false
                viewModel.changePassword(UserChangePasswordRequest(newPassword))
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        var isValid = true
        val passwordRegex = UserValidator.passwordRegex()

        if (newPassword.isEmpty()) {
            showError(binding.tvNewPasswordError, "La contraseña es requerida")
            isValid = false
        } else if (!passwordRegex.matches(newPassword)) {
            showError(binding.tvNewPasswordError, "La contraseña no cumple los requisitos")
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            showError(binding.tvConfirmPasswordError, "Confirme la contraseña")
            isValid = false
        } else if (newPassword != confirmPassword) {
            showError(binding.tvConfirmPasswordError, "Las contraseñas no coinciden")
            isValid = false
        }

        return isValid
    }

    private fun showError(textView: android.widget.TextView, message: String) {
        textView.text = message
        textView.isVisible = true
    }

    private fun clearErrors() {
        binding.tvNewPasswordError.isVisible = false
        binding.tvConfirmPasswordError.isVisible = false
    }

    private fun observeViewModel() {
        viewModel.logoutState.observe(viewLifecycleOwner) { state ->
            // Solo procesar si estamos en proceso de cambio de contraseña
            if (!isChangingPassword) return@observe

            when (state) {
                is Resource.Success -> {
                    // Sesión cerrada exitosamente después del cambio de contraseña
                    Toast.makeText(
                        context,
                        "Contraseña cambiada exitosamente. Inicie sesión nuevamente.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navegar al login y limpiar el back stack
                    findNavController().navigate(
                        R.id.action_global_loginFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph_host, true)
                            .build()
                    )
                }

                is Resource.Error -> {
                    isChangingPassword = false
                    binding.btnChangePassword.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }

                else -> Unit
            }
        }

        viewModel.userData.observe(viewLifecycleOwner) { state ->
            // Solo procesar errores de validación
            if (!isChangingPassword) return@observe

            when (state) {
                is Resource.Error -> {
                    isChangingPassword = false
                    binding.btnChangePassword.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }

                is Resource.ValidationError -> {
                    isChangingPassword = false
                    binding.btnChangePassword.isEnabled = true
                    state.errors["password"]?.let {
                        showError(binding.tvNewPasswordError, it)
                    }
                }

                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}