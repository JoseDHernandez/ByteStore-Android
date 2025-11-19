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
import com.example.bytestore.core.ApiClient
import com.example.bytestore.R
import android.util.Log

class CartAdapter(
    private val onInc: (CartItemModel) -> Unit,
    private val onDec: (CartItemModel) -> Unit
) : ListAdapter<CartItemModel, CartAdapter.VH>(DIFF) {

    inner class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: CartItemModel) {
            Log.d("CartAdapter", "inding item: id=${item.id}, productId=${item.productId}, name='${item.name}', image='${item.image}'")

            // Mostrar nombre completo del producto
            b.txtName.text = item.name.ifEmpty { "Producto sin nombre" }
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
                old.productId == new.productId // Comparar por productId, no por id

            override fun areContentsTheSame(old: CartItemModel, new: CartItemModel) =
                old == new
        }
    }
}