package com.example.bytestore.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.data.model.user.UserLoginRequest
import com.example.bytestore.databinding.FragmentLoginBinding
import com.example.bytestore.ui.viewmodel.AccountViewModel
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.utils.Resource
import com.google.android.material.snackbar.Snackbar


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding?=null
    private val binding get()=_binding!!

    private val viewModel: AccountViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //ir al registro
        binding.buttonSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        //ingresar
        binding.buttonSignIn.setOnClickListener {
            viewModel.loginUser(binding.inputEmail.text.toString(),binding.inputPassword.text.toString())
        }
        viewModel.accountState.observe(viewLifecycleOwner){state->
            when (state){
                is Resource.Idle-> Unit
                is Resource.Loading -> {
                    binding.buttonSignIn.isEnabled = false

                }
                is Resource.Success -> {
                    binding.buttonSignIn.isEnabled = true
                   findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                }
                is Resource.ValidationError -> {
                    binding.buttonSignIn.isEnabled = true

                }
                is Resource.Error ->{
                    binding.buttonSignIn.isEnabled = true

                }
            }
        }
    }

    override fun onDestroyView() {
        _binding=null
        super.onDestroyView()
    }
}