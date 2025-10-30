package com.example.bytestore.ui.components.navbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
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
        //obtener atributos
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.BottonNav)
        activeIndex = attributes.getInteger(R.styleable.BottonNav_actualFragment, 0)
        //establecer item activo
        setActiveItem(activeIndex)
        //listeners
        binding.itemProducts.setOnClickListener {
           onItemSelected?.invoke(R.id.action_global_productsFragment)
        }
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
        attributes.recycle()
    }


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
            val i = if (index <= items.size) index else 0
            items.getOrNull(i)?.apply {
                visibility = VISIBLE
            }
            activeIndex = i
        }
    }

}