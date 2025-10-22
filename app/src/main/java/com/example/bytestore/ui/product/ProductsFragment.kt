package com.example.bytestore.ui.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentProductsBinding
import com.example.bytestore.ui.viewmodel.ProductViewModel
import com.example.bytestore.utils.Resource


class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()

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
        productAdapter = ProductsListAdapter()
        //configuracion del recycleview
        binding.productsRecyclerView.adapter = productAdapter
        binding.productsRecyclerView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL )
        //obtener productos
        viewModel.productState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Idle -> Unit
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    productAdapter.submitList(state.data?.data ?:emptyList())
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.errorMessage.text = "Error en la carga de prodcutos"
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorLayout.visibility = View.GONE
                }

                is Resource.ValidationError -> {
                    //
                }
            }
        }
        viewModel.getProducts()
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}