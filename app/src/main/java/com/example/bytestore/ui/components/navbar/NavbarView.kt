package com.example.bytestore.ui.components.navbar

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
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
        //listeners
        binding.itemProducts.setOnClickListener {
            onItemSelected?.invoke(R.id.action_global_productsFragment)
        }
        binding.itemOrders.setOnClickListener {
            onItemSelected?.invoke(R.id.action_global_orderFragment)
        }
        binding.itemCart
        binding.itemOptions.setOnClickListener {
            val bottomSheet = OptionsBottomSheet()
            bottomSheet.onOptionSelected = { option ->
                when (option) {
                    "account" -> onItemSelected?.invoke(R.id.profileFragment)
                    "logout" -> onLogoutSelected?.invoke()
                }

            }
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "optionsSheet")
        }
        //TODO: pendiente de los otros fragments
    }

    //habilita o deshabilita el boton de opciones
    fun disableOptionsButton(state: Boolean) {
        binding.itemOptions.isEnabled = state
        if (!state) {
            binding.itemOptionsIcon.setColorFilter(
                ContextCompat.getColor(context, R.color.gray),
                PorterDuff.Mode.SRC_IN
            )
            binding.itemOptionsLabel.setTextColor(resources.getColor(R.color.gray, null))
        } else {
            binding.itemOptionsIcon.setColorFilter(
                ContextCompat.getColor(context, R.color.black),
                PorterDuff.Mode.SRC_IN
            )
            binding.itemOptionsLabel.setTextColor(resources.getColor(R.color.black, null))
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