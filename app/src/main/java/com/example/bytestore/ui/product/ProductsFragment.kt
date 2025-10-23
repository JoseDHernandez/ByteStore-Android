package com.example.bytestore.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentProductsBinding
import com.example.bytestore.ui.components.GridSpacingItemDecorator
import com.example.bytestore.ui.viewmodel.ProductViewModel
import com.example.bytestore.utils.Resource


class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()

    //Paginación
    private var currentPage = 1;
    private var hasNextPage = true;
    private var totalPages = 1;
    private var isLoading = false;
    private lateinit var productAdapter: ProductsListAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //boton de regreso
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        //Listadapater de prodcutos
        productAdapter = ProductsListAdapter()
        //configuracion del recycleview
        binding.productsRecyclerView.adapter = productAdapter
        val layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        binding.productsRecyclerView.layoutManager = layoutManager
        //espaciado entre tarjetas
        binding.productsRecyclerView.addItemDecoration(
            GridSpacingItemDecorator(
                2,
                resources.getDimensionPixelSize(R.dimen.grid_spacing),
                false
            )
        )
        //infinte scroll
        infiniteScroll(layoutManager)
        //obtener productos
        observeLiveData()
        //solicitar productos
        viewModel.getProducts()
    }

    private fun observeLiveData() {
        viewModel.productState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Idle -> Unit
                is Resource.Success -> {
                    binding.errorLayout.visibility = View.GONE
                    //paginas totales
                    totalPages = state.data.pages

                    val result = state.data

                    if (currentPage == 1) {
                        productAdapter.submitList(result.data)
                    } else {
                        //agregar los productos de la pagina sigiente con la lista actual
                        val currentList = productAdapter.currentList.toMutableList()
                        currentList.addAll(result.data)
                        productAdapter.submitList(currentList.toList())
                    }

                    //estado de la paginacion
                    hasNextPage = result.next != null && result.next > currentPage
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Error -> {
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.errorMessage.text = "Error en la carga de prodcutos"
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    if (currentPage == 1) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    binding.errorLayout.visibility = View.GONE
                }

                is Resource.ValidationError -> {
                    //
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun infiniteScroll(layoutManager: StaggeredGridLayoutManager) {
        binding.productsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || isLoading || !hasNextPage) return // scrol hacia arriba

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                //calcular items visibles
                val firstVisibleItemPositions = IntArray(2)
                layoutManager.findFirstVisibleItemPositions(firstVisibleItemPositions)
                val firstVisibleItem = firstVisibleItemPositions.minOrNull() ?: 0

                val endReached = (visibleItemCount + firstVisibleItem) >= totalItemCount - 4

                if (endReached) {
                    loadNextPage()
                }

            }
        })
    }

    private fun loadNextPage() {
        isLoading = true;
        currentPage++ //aumentar pagina
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