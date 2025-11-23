package com.example.bytestore.ui.admin.product.processor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentAdminProcessorsBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.adminViewModels.AdminProcessorsViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar


class AdminProcessorsFragment : ProtectedFragment() {
    override val requiredRole = "ADMINISTRADOR"

    //spinners
    var brandsOptionsList = mutableListOf(
        "Marcas"
    )
    var seriesOptionsList = mutableListOf(
        "Series"
    )
    var coresOptionsList = mutableListOf(
        "Núcleos"
    )

    private var _binding: FragmentAdminProcessorsBinding? = null
    private val binding get() = _binding!!

    //viewmodel
    private val viewModel: AdminProcessorsViewModel by viewModels()

    //adapter
    private lateinit var processorsAdapter: ProcessorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProcessorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Procesadores")
        setData()
    }

    private fun setData() {
        //recyclerview
        processorsAdapter = ProcessorAdapter { processor ->
            val action =
                AdminProcessorsFragmentDirections.actionAdminProcessorsFragmentToAdminProcessorFragment(
                    processor.id!!
                )
            findNavController().navigate(action)
        }
        val processorsList = binding.recyclerView
        processorsList.setHasFixedSize(true)
        processorsList.adapter = processorsAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        processorsList.layoutManager = layoutManager
        viewModel.getProcessors()
        observeData()

    }

    private fun observeData() {
        viewModel.processorsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> {
                    binding.errorLayout.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    //datos
                    val data = state.data
                    brandsOptionsList.addAll(data.map { it.brand }.distinct())
                    seriesOptionsList.addAll(data.map { it.family }.distinct())
                    coresOptionsList.addAll(data.map { it.cores.toString() }.distinct())
                    setDataSpinners()
                    //recyclerview
                    processorsAdapter.submitList(data.toMutableList())
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.addProcessor.visibility = View.VISIBLE
                }

                is Resource.Loading -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.addProcessor.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.addProcessor.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                }

                else -> Unit
            }
        }
    }

    private fun setDataSpinners() {
        //spinner
        val brandsSpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            brandsOptionsList
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        val seriesSpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            seriesOptionsList
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        val coresSpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            coresOptionsList
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        binding.brandSpinner.adapter = brandsSpinnerAdapter
        binding.seriesSpinner.adapter = seriesSpinnerAdapter
        binding.coresSpinner.adapter = coresSpinnerAdapter
        //TODO: pendiente logíca de condicional
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroy()
    }
}