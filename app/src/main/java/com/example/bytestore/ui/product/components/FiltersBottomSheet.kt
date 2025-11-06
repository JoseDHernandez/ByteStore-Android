package com.example.bytestore.ui.product.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val onApply: (selectedBrands: List<String>, selectedProcessors: List<String>, selectedDisplays: List<String>, selectedOrder: String) -> Unit
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
            onApply(
                viewModel.selectedBrands.value?.toList() ?: emptyList(),
                viewModel.selectedProcessors.value?.toList() ?: emptyList(),
                viewModel.selectedDisplays.value?.toList() ?: emptyList(),
                //TODO: pendiente pasar el tipo de ordenamiento a viewmodel
                binding.orderSpinner.selectedItem.toString() //tipo de ordenamiento seleccionado
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
        //opciones del spinner
        val orderOptions = listOf(
            "Ordernar por",
            "Relevancia",
            "Precio: menor a mayor",
            "Precio: mayor a menor"
        )

        //asignar datos y estilos al spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, //estilo del item
            orderOptions
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item) //estilo del spinner
        }
        binding.orderSpinner.adapter = spinnerAdapter
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