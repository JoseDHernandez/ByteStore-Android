package com.example.bytestore.ui.admin.product

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bytestore.R
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.databinding.ItemProductAdminBinding
import java.text.NumberFormat
import java.util.Locale

class ProductAdminAdapter(
    private val onEdit: (ProductModel) -> Unit,
    private val onDelete: (ProductModel) -> Unit
) : ListAdapter<ProductModel, ProductAdminAdapter.ProductViewHolder>(ProductDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(product: ProductModel) {
            val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))

            binding.productName.text = product.name
            binding.productModel.text = "Modelo: ${product.model}"
            binding.productBrand.text = "Marca: ${product.brand}"
            binding.productPrice.text = "$${formatter.format(product.price)}"
            binding.productStock.text = "Stock: ${product.stock}"

            // Cargar imagen
            val url = product.image.replace("localhost", "10.0.2.2")
            Glide.with(binding.productImage.context)
                .load(url)
                .placeholder(R.drawable.placeholder)
                .into(binding.productImage)

            // Botones de acción
            binding.btnEdit.setOnClickListener {
                onEdit(product)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(product)
            }
        }
    }

    private class ProductDiff : DiffUtil.ItemCallback<ProductModel>() {
        override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem == newItem
        }
    }
}
