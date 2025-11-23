package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bytestore.R
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.databinding.FragmentAdminUserBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.userViewModels.AdminUsersViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar
import kotlinx.coroutines.launch

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
        //informacion del usuario
        userId = fragmentArgs.userId
        viewModel.getUser(userId)
        viewModel.userSate.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                setData(state.data)
            }
        }
        //editar cuenta
        binding.accountEditButton.setOnClickListener {
            val action =
                AdminUserFragmentDirections.actionAdminUserFragmentToAdminUserUpdateFragment(userId)
            findNavController().navigate(action)
        }
        //eliminar
        binding.accountDeleteButton.setOnClickListener {
            showConfirmDeleteUser()
        }
    }

    private fun setData(user: UserModel) {
        binding.accountId.text = user.id
        binding.accountName.text = user.name
        binding.accountEmail.text = user.email
        binding.accountAddress.text = user.physicalAddress
        binding.accountRol.text = user.role
    }

    private fun showConfirmDeleteUser() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_ByteStore_AlertDialog)
            .setTitle("Eliminar usuario")
            .setMessage("¿Esta seguro de eliminar este usuario del sistema?")
            .setPositiveButton("Eliminar", { _, _ ->
                lifecycleScope.launch { deleteUser() }
            })
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
    }

    private suspend fun deleteUser() {
        val response = viewModel.deleteUser(userId)
        if (response) {
            Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_adminUserFragment_to_adminUsersFragment)
        } else {
            Toast.makeText(requireContext(), "Usuario no eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        userId = fragmentArgs.userId
        viewModel.getUser(userId)

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}