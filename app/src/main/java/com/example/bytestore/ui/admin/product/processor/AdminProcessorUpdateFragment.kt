package com.example.bytestore.ui.admin.product.processor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytestore.databinding.FragmentAdminProcessorUpdateBinding
import com.example.bytestore.ui.ProtectedFragment


class AdminProcessorUpdateFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding : FragmentAdminProcessorUpdateBinding?=null
    private val binding get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProcessorUpdateBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onDestroyView() {
       _binding = null
        super.onDestroyView()
    }

}