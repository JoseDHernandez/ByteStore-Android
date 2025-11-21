package com.example.bytestore.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentAdminBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.utils.topBar

class AdminFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"

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

        setupNavigations()
    }

    private fun setupNavigations() {
        // Navegación a productos
        binding.btnProducts.setOnClickListener {
            val action = AdminFragmentDirections.actionAdminFragmentToProductListAdminFragment()
            findNavController().navigate(action)
        }

        // TODO: Agregar navegaciones para otros módulos
        // binding.btnComments.setOnClickListener { ... }
        // binding.btnOrders.setOnClickListener { ... }
        // binding.btnUsers.setOnClickListener { ... }
        // binding.btnProcessros.setOnClickListener { ... }
        // binding.btnGraphics.setOnClickListener { ... }
        // binding.btnBrands.setOnClickListener { ... }
        // binding.btnOS.setOnClickListener { ... }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
