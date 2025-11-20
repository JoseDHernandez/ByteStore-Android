package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytestore.databinding.FragmentAdminUsersBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.utils.topBar


class AdminUsersFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Usuarios")
        //TODO:pendiente logica
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}