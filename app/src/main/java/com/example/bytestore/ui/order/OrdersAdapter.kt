 package com.example.bytestore.ui.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bytestore.R

data class OrderItem(
    val id: String,
    val createdDate: String,
    val deliveredDate: String,
    val productTitle: String,
    val productSubtitle: String,
    val price: String,
    val status: String
)

class OrdersAdapter(private val onClick: (OrderItem) -> Unit) :
    ListAdapter<OrderItem, OrdersAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OrderItem>() {
            override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        private val tvDeliveredDate: TextView = itemView.findViewById(R.id.tvDeliveredDate)
        private val tvProductTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        private val tvProductSubtitle: TextView = itemView.findViewById(R.id.tvProductSubtitle)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)

        fun bind(item: OrderItem) {
            tvOrderId.text = item.id
            tvCreatedDate.text = item.createdDate
            tvDeliveredDate.text = item.deliveredDate
            tvProductTitle.text = item.productTitle
            tvProductSubtitle.text = item.productSubtitle
            tvPrice.text = item.price
            tvStatus.text = item.status
            // placeholder image
            Glide.with(ivProduct.context).load(R.drawable.placeholder).into(ivProduct)

            itemView.setOnClickListener { onClick(item) }
        }
    }
}

