package com.example.bytestore.ui.admin.product

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentProductListAdminBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.productViewModels.ProductCrudViewModel
import com.example.bytestore.ui.viewmodel.productViewModels.ProductViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar

class ProductListAdminFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"

    private var _binding: FragmentProductListAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private val crudViewModel: ProductCrudViewModel by viewModels()

    private lateinit var productAdapter: ProductAdminAdapter

    // Paginación
    private var currentPage = 1
    private var hasNextPage = true
    private var totalPages = 1
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Gestión de Productos")

        setupRecyclerView()
        setupObservers()
        setupButtons()

        // Cargar productos
        viewModel.getProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdminAdapter{ product ->
            Log.d("ProductListAdminFragment","id: ${product.id}")
            val action = ProductListAdminFragmentDirections.actionProductListAdminFragmentToAdminProductFragment(product.id)
            findNavController().navigate(action)
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0 || isLoading || !hasNextPage) return

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 4) {
                        loadNextPage()
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        // Observar lista de productos
        viewModel.productsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    if (currentPage == 1) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.productsRecyclerView.visibility = View.GONE
                    }
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.productsRecyclerView.visibility = View.VISIBLE
                    binding.errorLayout.visibility = View.GONE

                    val result = state.data
                    totalPages = result.pages

                    val newList = if (currentPage == 1) {
                        result.data.toMutableList()
                    } else {
                        (productAdapter.currentList + result.data)
                            .distinctBy { it.id }
                            .toMutableList()
                    }

                    productAdapter.submitList(newList)
                    isLoading = false
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.productsRecyclerView.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.errorMessage.text = state.message
                    isLoading = false
                }

                else -> Unit
            }
        }

        // Observar eliminación de producto
        crudViewModel.deleteProductState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    // Mostrar loading
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT)
                        .show()
                    // Recargar lista
                    currentPage = 1
                    viewModel.getProducts()
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    private fun setupButtons() {
        // Botón para agregar producto
        binding.fabAddProduct.setOnClickListener {
            val action = ProductListAdminFragmentDirections
                .actionProductListAdminFragmentToProductRegisterFragment()
            findNavController().navigate(action)
        }

        // Botón de recargar en caso de error
        binding.retryButton.setOnClickListener {
            currentPage = 1
            viewModel.getProducts()
        }
    }

    private fun loadNextPage() {
        if (isLoading || !hasNextPage) return
        isLoading = true
        currentPage++
        if (currentPage > totalPages) {
            hasNextPage = false
        } else {
            viewModel.getProducts(currentPage)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}