package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytestore.databinding.FragmentAdminUserBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.utils.topBar

class AdminUserFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding: FragmentAdminUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Gestión de usuario")
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}