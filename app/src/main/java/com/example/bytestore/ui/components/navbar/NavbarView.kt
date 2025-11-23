package com.example.bytestore.ui.components.navbar

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bytestore.R
import com.example.bytestore.databinding.ViewBottomNavBinding

class NavbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var onItemSelected: ((Int) -> Unit)? = null
    var onLogoutSelected: (() -> Unit)? = null

    private val binding = ViewBottomNavBinding.inflate(LayoutInflater.from(context), this, true)
    private var activeIndex = 0

    init {
        // Botón de productos
        binding.itemProducts.setOnClickListener {
            onItemSelected?.invoke(R.id.action_global_productsFragment)
        }

        // Botón del carrito
        binding.itemCart.setOnClickListener {
            onItemSelected?.invoke(R.id.cartFragment)
        }

        // Botón de órdenes (habilitado)
        binding.itemOrders.setOnClickListener {
            onItemSelected?.invoke(R.id.ordersFragment)
        }

        // Botón de opciones
        binding.itemOptions.setOnClickListener {
            val bottomSheet = OptionsBottomSheet()
            bottomSheet.onOptionSelected = { option ->
                when (option) {
                    "account" -> onItemSelected?.invoke(R.id.profileFragment)
                    "logout" -> onLogoutSelected?.invoke()
                    "admin" -> onItemSelected?.invoke(R.id.adminFragment)
                }
            }
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "optionsSheet")
        }
    }

    //habilita o deshabilita el boton de opciones
    fun disableOptionsButtons(enabled: Boolean) {

        binding.itemOptions.isEnabled = enabled
        binding.itemOrders.isEnabled = enabled
        binding.itemCart.isEnabled = enabled

        val color = if (enabled) R.color.black else R.color.gray
        val tintColor = ContextCompat.getColor(context, color)

        //aplicar colores a iconos
        listOf(
            binding.itemOptionsIcon,
            binding.itemCartIcon,
            binding.itemOrdersIcon
        ).forEach { icon ->
            icon.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        }

        //aplicar colores a textos
        listOf(
            binding.itemOptionsLabel,
            binding.itemCartLabel,
            binding.itemOrdersLabel
        ).forEach { label ->
            label.setTextColor(tintColor)
        }
    }


    //indica cual item esta activo (gestionar desde el MainActivity)
    fun setActiveItem(index: Int) {
        if (index >= 0 && index <= 3) {
            val items = listOf(
                binding.activeProducts,
                binding.activeOrders,
                binding.activeCart,
                binding.activeOptions
            )
            items.forEach { item ->
                item.visibility = INVISIBLE
            }
            items.getOrNull(index)?.apply {
                visibility = VISIBLE
            }
            activeIndex = index
        }
    }

}
