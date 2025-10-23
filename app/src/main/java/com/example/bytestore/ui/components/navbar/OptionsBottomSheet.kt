package com.example.bytestore.ui.components.navbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.bytestore.databinding.OptionsBottomSheetBinding
import com.example.bytestore.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class OptionsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var sessionManager: SessionManager
    private var _binding: OptionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    var onOptionSelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OptionsBottomSheetBinding.inflate(inflater, container, false)
        //validar rol
        sessionManager = SessionManager(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            val isAdmin = sessionManager.checkAccess("ADMINISTRADOR")
            binding.btnAdmin.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }
        //instalar listeners de las opciones
        setupListeners()
        return binding.root
    }


    private fun setupListeners() {
        binding.close.setOnClickListener {
            dismiss()
        }
        binding.btnAccount.setOnClickListener {
            onOptionSelected?.invoke("account")
            dismiss()
        }

        binding.btnLogout.setOnClickListener {
            onOptionSelected?.invoke("logout")
            dismiss()
        }

        binding.btnAdmin.setOnClickListener {
            onOptionSelected?.invoke("admin")
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}