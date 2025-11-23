package com.example.bytestore.ui.admin.product.processor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytestore.databinding.FragmentAdminProcessorBinding
import com.example.bytestore.ui.ProtectedFragment

class AdminProcessorFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding: FragmentAdminProcessorBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProcessorBinding.inflate(inflater, container, false)
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