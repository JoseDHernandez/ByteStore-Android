package com.example.bytestore.ui.admin.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentAdminUsersBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.userViewModels.AdminUsersViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar


class AdminUsersFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminUsersViewModel by viewModels()

    //Paginación
    private var currentPage = 1;
    private var hasNextPage = true;
    private var totalPages = 1;
    private var isLoading = false;
    private lateinit var usersAdapter: AdminUsersAdapter
    private var query: String? = null

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
        viewModel.getUsers()
        setAdapterAndRecyclerView(savedInstanceState)
        //nuevo usuario
        binding.newUserButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminUsersFragment_to_adminUserRegisterFragment)
        }
        //buscar usuario
        binding.searchButton.setOnClickListener {
            val q = binding.searchInput.toString().trim()
            val regex = Regex("^[0-9A-Za-zÁÉÍÓÚáéíóúÑñ\\s@.]+$")
            if (q.matches(regex)) {
                Toast.makeText(requireContext(), "Busqueda invalida", Toast.LENGTH_SHORT).show()
            } else {
                query = q
                currentPage = 1
                totalPages = 1
                hasNextPage = true
                isLoading = false
                usersAdapter.submitList(emptyList())
                viewModel.getUsers(1, query)
                binding.recyclerView.scrollToPosition(0)
            }
        }
        //mostrar limpiar
        binding.searchInput.addTextChangedListener { editable ->
            val text = editable?.toString() ?: ""
            if (text.length > 1) {
                binding.clearSearchButton.visibility = View.VISIBLE
            } else {
                binding.clearSearchButton.visibility = View.GONE
            }

        }
        //limpiar
        binding.clearSearchButton.setOnClickListener { clearSearch() }
    }

    private fun clearSearch() {
        query = null
        currentPage = 1
        totalPages = 1
        hasNextPage = true
        binding.searchInput.setText("")
        usersAdapter.submitList(emptyList())
        viewModel.getUsers(1)
    }

    private fun setAdapterAndRecyclerView(savedInstanceState: Bundle?) {
        usersAdapter = AdminUsersAdapter { user ->
            val action =
                AdminUsersFragmentDirections.actionAdminUsersFragmentToAdminUserFragment(user.id)
            findNavController().navigate(action)
        }
        //adapter
        val usersList = binding.recyclerView
        usersList.setHasFixedSize(true)
        usersList.adapter = usersAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        usersList.layoutManager = layoutManager
        infiniteScroll(layoutManager)
        observeLiveData()
        viewModel.getUsers()
    }

    private fun observeLiveData() {
        viewModel.usersState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Idle -> Unit
                is Resource.Success -> {
                    binding.errorLayout.visibility = View.GONE
                    binding.errorSearchLayout.visibility = View.GONE
                    val result = state.data
                    totalPages = result.pages

                    val newList = if (currentPage == 1) {
                        result.data.toMutableList()
                    } else {
                        (usersAdapter.currentList + result.data)
                            .distinctBy { it.id }
                            .toMutableList()
                    }

                    usersAdapter.submitList(newList.toMutableList())
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.errorSearchLayout.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.errorMessage.text = state.message
                    binding.errorLayout.visibility = View.VISIBLE
                }

                is Resource.ValidationError -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    val errors = state.errors
                    errors["search"]?.let { binding.errorSearchMessage.text = it }
                    binding.errorSearchLayout.visibility = View.VISIBLE
                }

                is Resource.Loading -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorLayout.visibility = View.GONE
                    binding.errorSearchLayout.visibility = View.GONE
                    if (currentPage == 1) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun infiniteScroll(layoutManager: LinearLayoutManager) {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || isLoading || !hasNextPage) return // scrol hacia arriba

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                //calcular items visibles
                val firstVisibleItemPositions = IntArray(2)
                layoutManager.findFirstCompletelyVisibleItemPosition()
                val firstVisibleItem = firstVisibleItemPositions.minOrNull() ?: 0

                val endReached = (visibleItemCount + firstVisibleItem) >= totalItemCount - 4

                if (endReached) {
                    loadNextPage()
                }

            }
        })
    }

    private fun loadNextPage() {
        if (isLoading || !hasNextPage) return
        isLoading = true;
        currentPage++ //aumentar pagina
        if (currentPage > totalPages) {
            hasNextPage = false
        } else {

            viewModel.getUsers(
                currentPage,
                query
            )
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}