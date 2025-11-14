package com.example.bytestore.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentCartBinding
import com.example.bytestore.ui.ProtectedFragment
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.CartViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar

class CartFragment : ProtectedFragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val vm: CartViewModel by activityViewModels { AppViewModelFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Carrito")

        setupRecyclerView()

        // Asegurar que cualquier checkout temporal se cierre antes de observar el carrito
        vm.finishTemporaryCheckout()

        observeCart()
        setupClickListeners()

        // Cargar datos locales
        vm.loadLocalSnapshot()
    }

    override fun onResume() {
        super.onResume()
        vm.loadLocalSnapshot()
    }

    private fun setupRecyclerView() {
        val adapter = CartAdapter(
            onInc = {
                Log.d("CartFragment", "Incrementar producto: ${it.productId}")
                vm.inc(it.productId.toLong())
            },
            onDec = {
                Log.d("CartFragment", "Decrementar producto: ${it.productId}")
                vm.dec(it.productId.toLong())
            }
        )
        binding.recycler.adapter = adapter
        binding.recycler.setHasFixedSize(true)

        //  SWIPE PARA ELIMINAR - Versión corregida
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_eliminate)
        val backgroundPaint = Paint().apply {
            color = R.color.gray
            isAntiAlias = true
        }

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = adapter.currentList.getOrNull(pos)
                    if (item != null) {
                        Log.d("CartFragment", "Swipe eliminar producto: ${item.productId}")
                        vm.remove(item.productId.toLong())
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView
                val icon = deleteIcon

                // Solo dibujar cuando se desliza hacia la izquierda
                if (dX < 0) {
                    // Calcular el tamaño del ícono
                    val iconSize = dpToPx(32)
                    val iconMargin = (itemView.height - iconSize) / 2

                    // Dibujar fondo rojo con bordes redondeados
                    val cornerRadius = dpToPx(16).toFloat()
                    val backgroundRect = RectF(
                        itemView.right.toFloat() + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    c.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)

                    // Posicionar y dibujar el ícono
                    val iconTop = itemView.top + iconMargin
                    val iconBottom = iconTop + iconSize
                    val iconRight = itemView.right - iconMargin
                    val iconLeft = iconRight - iconSize

                    icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon?.setTint(Color.WHITE)
                    icon?.draw(c)
                }
            }
        }

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(binding.recycler)
    }

    private fun observeCart() {
        vm.state.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val cartState = resource.data

                    Log.d("CartFragment", "Carrito actualizado - Items: ${cartState.items.size}")
                    cartState.items.forEachIndexed { index, item ->
                        Log.d("CartFragment", "  [$index] id=${item.id} productId=${item.productId} ${item.name} - Qty: ${item.quantity}")
                    }

                    val adapter = binding.recycler.adapter as CartAdapter
                    val previousSize = adapter.currentList?.size ?: 0

                    adapter.submitList(cartState.items) {
                        val newSize = cartState.items.size
                        if (newSize > previousSize) {
                            binding.recycler.post {
                                binding.recycler.smoothScrollToPosition(newSize - 1)
                            }
                        }
                    }

                    // Mostrar RecyclerView si hay items
                    binding.recycler.isVisible = cartState.items.isNotEmpty()
                }
                is Resource.Error -> {
                    Log.e("CartFragment", "Error al cargar carrito: ${resource.message}")
                    binding.recycler.isVisible = false
                }
                is Resource.Loading -> {
                    Log.d("CartFragment", "Cargando carrito...")
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCheckout.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_cart_to_checkout)
            } catch (e: Exception) {
                Log.e("CartFragment", "Error navegando a checkout: ${e.message}")
            }
        }

        binding.btnContinue.setOnClickListener {
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                Log.e("CartFragment", "Error al volver: ${e.message}")
            }
        }
    }

    // Función auxiliar para convertir dp a px
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}