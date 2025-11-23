package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bytestore.R
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.data.model.user.UserUpdateInputs
import com.example.bytestore.databinding.FragmentAdminUserUpdateBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.adminViewModels.AdminUsersViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.sessionManager
import com.example.bytestore.utils.topBar
import kotlinx.coroutines.launch
import java.util.Locale


class AdminUserUpdateFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding: FragmentAdminUserUpdateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminUsersViewModel by viewModels()
    private val fragmentArgs: AdminUserFragmentArgs by navArgs()
    private lateinit var user: UserModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Editar usuario")
        //datos del usuario
        setData()
        //botones
        binding.buttonSingUp.setOnClickListener { updateUser() }
        binding.cancelButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminUserUpdateFragment_to_adminUsersFragment)
        }
    }

    private fun setData() {
        //spinner
        val optionsList = listOf(
            "Seleccionar",
            "Cliente",
            "Administrador"
        )
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            optionsList
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        binding.spinnerRole.adapter = spinnerAdapter
        //obtener usuario
        viewModel.getUser(fragmentArgs.userId)
        //establecer datos
        viewModel.userSate.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    clearErrors()
                    user = state.data
                    //validar si es el mismo usuario
                    lifecycleScope.launch { isSameUser() }
                    binding.inputName.setText(user.name)
                    binding.inputEmail.setText(user.email)
                    binding.inputAddress.setText(user.physicalAddress)
                    //rol
                    val position = spinnerAdapter.getPosition(
                        user.role
                            .lowercase()
                            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    )
                    if (position >= 0) binding.spinnerRole.setSelection(position, false)
                    //eventos del spinner
                    binding.spinnerRole.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val selected =
                                    parent?.getItemAtPosition(position).toString().uppercase()

                                if (selected != "SELECCIONAR" && selected != user.role) {
                                    viewModel.changeRol(user.id, selected)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                        }
                }

                else -> Unit
            }
        }
        //respuesta de actulizacion
        viewModel.userUpdateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Usuario actulizado", Toast.LENGTH_SHORT)
                        .show()
                    val action =
                        AdminUserUpdateFragmentDirections.actionAdminUserUpdateFragmentToAdminUserFragment(
                            user.id
                        )
                    //findNavController().navigate(action)
                    findNavController().navigateUp()
                }

                is Resource.ValidationError -> showValidationErrors(state.errors)
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Error al actulizar", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("AdminUserUpdateFragment", state.message)
                }

                else -> Unit
            }

        }
    }

    private fun updateUser() {
        val request = UserUpdateInputs(
            binding.inputName.text.toString().trim(),
            binding.inputEmail.text.toString().trim(),
            binding.inputAddress.text.toString().trim()
        )
        viewModel.updateUser(user.id, request)
    }

    //limpiar errores
    private fun clearErrors() {
        binding.inputNameMessage.text = ""
        binding.inputEmailMessage.text = ""
        binding.inputAddressMessage.text = ""
    }

    //validaciones
    private fun showValidationErrors(errors: Map<String, String>) {
        clearErrors()
        errors["name"]?.let { binding.inputNameMessage.text = it }
        errors["email"]?.let { binding.inputEmailMessage.text = it }
        errors["address"]?.let { binding.inputAddressMessage.text = it }
        binding.scrollView.scrollTo(0, 0)
    }

    private suspend fun isSameUser() {
        if (sessionManager().getCurrentUser()?.id == user.id) findNavController().navigate(R.id.action_adminUserUpdateFragment_to_profileFragment)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}