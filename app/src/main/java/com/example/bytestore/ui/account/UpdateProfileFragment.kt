package com.example.bytestore.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.data.model.user.UserUpdateInputs
import com.example.bytestore.databinding.FragmentUpdateProfileBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.userViewModels.AccountViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar

class UpdateProfileFragment : ProtectedFragment() {

    private var _binding: FragmentUpdateProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    private var isUpdating = false
    private var originalName: String? = null
    private var originalEmail: String? = null
    private var originalAddress: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Actualizar datos")

        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { state ->
            when {
                // Cargar datos iniciales (cuando NO está actualizando)
                !isUpdating && state is Resource.Success -> {
                    state.data?.let { user ->
                        // Guardar valores originales
                        originalName = user.name
                        originalEmail = user.email
                        originalAddress = user.physicalAddress

                        // Mostrar datos en los campos
                        binding.etName.setText(user.name ?: "")
                        binding.etEmail.setText(user.email ?: "")
                        binding.etAddress.setText(user.physicalAddress ?: "")
                    }
                }

                // Procesar respuesta de actualización
                isUpdating -> {
                    when (state) {
                        is Resource.Success -> {
                            isUpdating = false
                            Toast.makeText(
                                context,
                                "Datos actualizados correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }

                        is Resource.Error -> {
                            isUpdating = false
                            Toast.makeText(
                                context,
                                state.message ?: "Error al actualizar",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is Resource.ValidationError -> {
                            isUpdating = false
                            state.errors["name"]?.let {
                                showError(binding.tvNameError, it)
                            }
                            state.errors["email"]?.let {
                                showError(binding.tvEmailError, it)
                            }
                            state.errors["address"]?.let {
                                showError(binding.tvAddressError, it)
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            clearErrors()

            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            // Validación básica
            if (name.isEmpty()) {
                showError(binding.tvNameError, "El nombre es requerido")
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                showError(binding.tvEmailError, "El correo electrónico es requerido")
                return@setOnClickListener
            }

            // Validación de formato de email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(binding.tvEmailError, "Formato de correo electrónico inválido")
                return@setOnClickListener
            }

            // Verificar si algo cambió
            val nameChanged = name != originalName
            val emailChanged = email != originalEmail
            val addressChanged = address != originalAddress

            if (!nameChanged && !emailChanged && !addressChanged) {
                Toast.makeText(context, "No hay cambios para actualizar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isUpdating = true

            // CRÍTICO: Enviar TODOS los campos (el backend los requiere)
            val inputs = UserUpdateInputs(
                name = name,
                email = email,
                address = address
            )

            viewModel.updateAccount(inputs)
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditAddress.setOnClickListener {
            binding.etAddress.requestFocus()
            binding.etAddress.setSelection(binding.etAddress.text.length)
        }
    }

    private fun showError(textView: android.widget.TextView, message: String) {
        textView.text = message
        textView.isVisible = true
    }

    private fun clearErrors() {
        binding.tvNameError.isVisible = false
        binding.tvEmailError.isVisible = false
        binding.tvAddressError.isVisible = false
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}