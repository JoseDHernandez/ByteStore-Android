package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.example.bytestore.databinding.FragmentAdminUserBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.userViewModels.AdminUsersViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar
import kotlin.getValue

class AdminUserFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private val fragmentArgs: AdminUserFragmentArgs by navArgs()
    private var _binding: FragmentAdminUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var userId: String //id del usuario
    private val viewModel: AdminUsersViewModel by viewModels()
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
        userId = fragmentArgs.userId
        viewModel.getUser(userId)
        viewModel.userSate.observe(viewLifecycleOwner) {state ->
            if(state is Resource.Success){
                val user = state.data
                binding.accountId.text = user.id
                binding.accountName.text = user.name
                binding.accountEmail.text = user.email
                binding.accountAddress.text = user.physicalAddress
                binding.accountRol.text = user.role
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}