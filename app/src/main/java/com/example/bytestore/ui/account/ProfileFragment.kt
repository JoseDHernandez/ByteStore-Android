package com.example.bytestore.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentProfileBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.userViewModels.AccountViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar

class ProfileFragment : ProtectedFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by viewModels {
        AppViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Cuenta")

        setupNavigation()
        observeUserData()
    }

    private fun setupNavigation() {
        // Navegación a Cambiar contraseña
        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }

        // Navegación a Actualizar datos
        binding.btnUpdateProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_updateProfileFragment)
        }
    }

    private fun observeUserData() {
        viewModel.userData.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success && state.data != null) {
                val user = state.data
                binding.accountId.text = user.id
                binding.accountName.text = user.name
                binding.accountEmail.text = user.email
                binding.accountAddress.text = user.physicalAddress
            } else {
                findNavController().navigate(R.id.action_profileFragment_to_mainFragment)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
