package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytestore.databinding.FragmentAdminUserRegisterBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.utils.topBar


class AdminUserRegisterFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding: FragmentAdminUserRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Registrar usuario")
        //TODO:pendiente logica
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}