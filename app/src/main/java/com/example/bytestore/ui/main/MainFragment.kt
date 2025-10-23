package com.example.bytestore.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //logica para cuando ya cargo la vista
        binding.buttonSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }
        //boton de registro
        binding.buttonSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_registerFragment)
        }
        //skip
        binding.skipSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_productsFragment)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}