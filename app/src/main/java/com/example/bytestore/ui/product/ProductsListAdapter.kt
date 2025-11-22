package com.example.bytestore.ui.product

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.bytestore.R
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.databinding.ItemProductCardBinding
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

class ProductsListAdapter (private val onItemClick: (ProductModel)->Unit):
    ListAdapter<ProductModel, ProductsListAdapter.ProductViewHolder>(ProductDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding =
            ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(product: ProductModel) {
            //formater
            val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
            binding.title.text = product.name
          binding.score.rating = product.qualification
           // binding.score.rating = Random.nextDouble(3.0,5.0).toFloat()
            val price =  "$${formatter.format(product.price)}"
            val discount = "$${formatter.format(product.price - (product.price * product.discount) / 100)}"
            binding.price.text = if(product.discount.toDouble() == 0.0) price else discount
            if(product.discount.toDouble() == 0.0) binding.discount.visibility = View.INVISIBLE
            binding.discount.text = price
            binding.discount.paintFlags = binding.discount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            //TODO: Cambiar Url cuando este
            val url = product.image.replace("localhost", "10.0.2.2")
            //imagen
            Glide.with(binding.image.context)
                .load(url) //url
                .override(300, 300) //redimension de las imagenes
                .placeholder(R.drawable.placeholder) //placeholder
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL) //cache (miniatura y original)
                .into(binding.image) //ubicación

            //onClick
            binding.root.setOnClickListener {
                onItemClick(product)
            }
        }

    }

    private class ProductDiff : DiffUtil.ItemCallback<ProductModel>() {
        override fun areItemsTheSame(
            oldItem: ProductModel,
            newItem: ProductModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ProductModel,
            newItem: ProductModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}