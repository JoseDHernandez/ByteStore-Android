package com.example.bytestore.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentAdminBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.utils.topBar

class AdminFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR" //solo el administrador puede ver el fragment

    private var _binding : FragmentAdminBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Gestión")
        //TODO: agregar navegaciones
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}