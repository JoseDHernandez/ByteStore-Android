package com.example.bytestore.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytestore.databinding.FragmentOrderBinding
import com.example.bytestore.ui.ProtectedFragment


class OrderFragment : ProtectedFragment() {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //input de la direccion
        binding.addressInput
        //boton de editar direccion
        binding.addressButton
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}