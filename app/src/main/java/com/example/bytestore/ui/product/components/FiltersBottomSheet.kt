package com.example.bytestore.ui.product.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.example.bytestore.R
import com.example.bytestore.databinding.ProductFiltersBottomSheetBinding
import com.example.bytestore.ui.components.GridSpacingItemDecorator
import com.example.bytestore.ui.viewmodel.productViewModels.ProductFiltersViewModel
import com.example.bytestore.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FiltersBottomSheet(
    private val onApply: (selectedFilters: List<String>, selectedOrder: Map<String,String>) -> Unit
) : BottomSheetDialogFragment() {
    private var _binding: ProductFiltersBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductFiltersViewModel by activityViewModels()

    private lateinit var brandsAdapter: ProductFiltersListAdapter
    private lateinit var processorsAdapter: ProductFiltersListAdapter
    private lateinit var displaysAdapter: ProductFiltersListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ProductFiltersBottomSheetBinding.inflate(inflater, container, false)
        //Expandir los filtros
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        //datos de ordenamiento
        setSpinnerData()
        //asignar datos
        setLiveData()
        //boton de cerrar
        binding.close.setOnClickListener {
            dismiss()
        }
        //aplicar filtros
        binding.appltFilters.setOnClickListener {
            val selectedFilters = listOfNotNull(
                viewModel.selectedBrands.value?.toList(),
               viewModel.selectedProcessors.value?.toList(),
                viewModel.selectedDisplays.value?.toList()
            ).flatten()
            onApply(
                selectedFilters,
                viewModel.getSelectedOrder()
            )
            dismiss()
        }
        //limpiar filtros
        binding.clearFilters.setOnClickListener {
            viewModel.clearSelections()
        }
        return binding.root
    }

    private fun setSpinnerData() {
        //asignar datos y estilos al spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, //estilo del item
            viewModel.orderOptions
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item) //estilo del spinner
        }
        binding.orderSpinner.adapter = spinnerAdapter
        //mantener seleccion
        binding.orderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent?.getItemAtPosition(position).toString()
                viewModel.setSelectedOrder(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        viewModel.selectedOrder.observe(viewLifecycleOwner) { order ->
            val position = spinnerAdapter.getPosition(order)
            if(position>=0) binding.orderSpinner.setSelection(position)
        }
    }
    private fun setLiveData() {
        viewModel.productFiltersState.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                val filter = state.data
                brandsAdapter = ProductFiltersListAdapter(filter.brands) {
                    viewModel.toggleBrandSelection(it)
                }
                processorsAdapter = ProductFiltersListAdapter(filter.processors) {
                    viewModel.toggleProcessorSelection(it)
                }
                displaysAdapter = ProductFiltersListAdapter(filter.displays) {
                    viewModel.toggleDisplaySelection(it)
                }
                //configurar los recycleviews
                val spacing = GridSpacingItemDecorator(2, 15, true)
                binding.recyclerViewBrands.apply {
                    adapter = brandsAdapter
                    addItemDecoration(spacing)
                }
                binding.recyclerViewProcessors.apply {
                    adapter = processorsAdapter
                    addItemDecoration(spacing)
                }
                binding.recyclerViewDisplays.apply {
                    adapter = displaysAdapter
                    addItemDecoration(spacing)
                }
                //observar selectores
                viewModel.selectedBrands.observe(viewLifecycleOwner) {
                    brandsAdapter.setSelectedItems(it)
                }
                viewModel.selectedProcessors.observe(viewLifecycleOwner) {
                    processorsAdapter.setSelectedItems(it)
                }
                viewModel.selectedDisplays.observe(viewLifecycleOwner) {
                    displaysAdapter.setSelectedItems(it)
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        //solicitar filtros
        viewModel.getProductFilters()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}