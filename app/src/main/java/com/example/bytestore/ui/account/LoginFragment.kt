package com.example.bytestore.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentLoginBinding
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.userViewModels.AuthViewModel
import com.example.bytestore.utils.Resource


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //saltar ingreso
        binding.skipSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_global_productsFragment)
        }
        //ir al registro
        binding.buttonSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        //ingresar
        binding.buttonSignIn.setOnClickListener {
            viewModel.loginUser(
                binding.inputEmail.text.toString(),
                binding.inputPassword.text.toString()
            )
        }
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Idle -> Unit
                is Resource.Loading -> {
                    binding.buttonSignIn.isEnabled = false
                    clearErrors()
                }

                is Resource.Success -> {
                    binding.buttonSignIn.isEnabled = true
                    findNavController().navigate(R.id.action_loginFragment_to_productsFragment)
                }

                is Resource.ValidationError -> {
                    binding.buttonSignIn.isEnabled = true
                    showErrors(state.errors)

                }

                is Resource.Error -> {
                    binding.buttonSignIn.isEnabled = true

                }
            }
        }
    }

    private fun clearErrors() {
        binding.inputEmailMessage.text = ""
        binding.inputPasswordMessage.text = ""
    }

    private fun showErrors(errors: Map<String, String>) {
        clearErrors()
        errors["email"]?.let { binding.inputEmailMessage.text = it }
        errors["password"]?.let { binding.inputPasswordMessage.text = it }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}