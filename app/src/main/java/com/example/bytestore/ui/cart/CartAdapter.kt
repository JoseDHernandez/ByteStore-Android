package com.example.bytestore.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bytestore.data.model.cart.CartItemModel
import com.example.bytestore.databinding.ItemCartBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.bytestore.R
import android.util.Log

class CartAdapter(
    private val onInc: (CartItemModel) -> Unit,
    private val onDec: (CartItemModel) -> Unit
) : ListAdapter<CartItemModel, CartAdapter.VH>(DIFF) {

    inner class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: CartItemModel) {
            Log.d("CartAdapter", "Binding item: id=${item.id}, productId=${item.productId}, name='${item.name}', image='${item.image}'")

            // Mostrar nombre completo del producto
            b.txtName.text = item.name.ifEmpty { "Producto sin nombre" }

            // Extraer especificaciones del nombre si están disponibles
            val specs = extractSpecifications(item.name)
            if (specs.isNotEmpty()) {
                b.txtSpecs.text = specs
                b.txtSpecs.visibility = android.view.View.VISIBLE
            } else {
                b.txtSpecs.visibility = android.view.View.GONE
            }

            b.txtQuantity.text = item.quantity.toString()
            b.txtPrice.text = item.formattedPrice

            // Listeners - Solo botones de incrementar/decrementar
            b.btnIncrease.setOnClickListener { onInc(item) }
            b.btnDecrease.setOnClickListener { onDec(item) }

            // Cargar imagen del producto
            val ctx = b.imgProduct.context
            var imageUrl = item.image

            // Reemplazar localhost por la IP del emulador y asegurar URL absoluta
            if (!imageUrl.isNullOrEmpty()) {
                imageUrl = imageUrl.replace("localhost", "10.0.2.2")

                Log.d("CartAdapter", "Cargando imagen: productId=${item.productId}")
                Log.d("CartAdapter", "   Original: '${item.image}'")
                Log.d("CartAdapter", "   Final: '$imageUrl'")

                Glide.with(ctx)
                    .load(imageUrl)
                    .override(150, 150)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(b.imgProduct)
            } else {
                Log.w("CartAdapter", "Imagen NULL para productId=${item.productId}")
                b.imgProduct.setImageResource(R.drawable.placeholder)
            }
        }

        /**
         * Extrae las especificaciones del nombre del producto
         * Busca patrones como "16GB", "1TB", "Intel Core i7", etc.
         */
        private fun extractSpecifications(name: String): String {
            val specs = mutableListOf<String>()

            // Buscar procesador (Intel Core, AMD Ryzen, M1, M2, etc.)
            val processorPatterns = listOf(
                Regex("Intel\\s+Core\\s+i\\d+", RegexOption.IGNORE_CASE),
                Regex("AMD\\s+Ryzen\\s+\\d+", RegexOption.IGNORE_CASE),
                Regex("Apple\\s+M\\d+", RegexOption.IGNORE_CASE),
                Regex("Apple\\s+Silicon", RegexOption.IGNORE_CASE)
            )

            for (pattern in processorPatterns) {
                val match = pattern.find(name)
                if (match != null) {
                    specs.add(match.value)
                    break
                }
            }

            // Buscar RAM (GB)
            val ramPattern = Regex("(\\d+)\\s*GB\\b", RegexOption.IGNORE_CASE)
            val ramMatch = ramPattern.find(name)
            if (ramMatch != null) {
                specs.add("${ramMatch.groupValues[1]}GB")
            }

            // Buscar almacenamiento (TB/SSD)
            val storagePattern = Regex("(\\d+)\\s*TB\\b", RegexOption.IGNORE_CASE)
            val storageMatch = storagePattern.find(name)
            if (storageMatch != null) {
                specs.add("${storageMatch.groupValues[1]}TB")
            }

            return specs.joinToString(" • ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCartBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<CartItemModel>() {
            override fun areItemsTheSame(old: CartItemModel, new: CartItemModel) =
                old.productId == new.productId

            override fun areContentsTheSame(old: CartItemModel, new: CartItemModel) =
                old == new
        }
    }
}
