package com.example.bytestore.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bytestore.databinding.FragmentOrdersBinding
import com.example.bytestore.ui.ProtectedFragment

class OrdersFragment : ProtectedFragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val ordersAdapter by lazy {
        OrdersAdapter { item ->
            // navigate with orderId argument using navController
            findNavController().navigate(
                com.example.bytestore.R.id.action_orders_to_order_detail,
                Bundle().apply { putString("orderId", item.id) }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = ordersAdapter

        // sample data
        val sample = listOf(
            OrderItem("LEXGLh0", "08 Oct 2025", "10 Oct 2025", "Lenovo IdeaPad AMD R5 - 16GB", "Amd Ryzen 5 • 512 GB", "$ 2.125.980", "En proceso"),
            OrderItem("0199c66b", "08 Oct 2025", "10 Oct 2025", "Lenovo IdeaPad AMD R5 - 16GB", "Amd Ryzen 5 • 512 GB", "$ 2.125.980", "Cancelado"),
            OrderItem("0199c66c", "08 Oct 2025", "10 Oct 2025", "Lenovo IdeaPad AMD R5 - 16GB", "Amd Ryzen 5 • 512 GB", "$ 2.125.980", "Entregado"),
            OrderItem("0199c66d", "08 Oct 2025", "12 Oct 2025", "Lenovo IdeaPad AMD R5 - 16GB", "Amd Ryzen 5 • 512 GB", "$ 2.125.980", "Retrasado")
        )
        ordersAdapter.submitList(sample)
        // TODO: Cargar órdenes desde ViewModel/Repository
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
