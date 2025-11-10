package com.example.bytestore.ui.product.components

import android.R
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.bytestore.data.model.product.ProductFilterItem
import com.example.bytestore.databinding.ItemFilterCheckboxBinding

class ProductFiltersListAdapter(
    private val items: List<ProductFilterItem>,
    private val onToggle: (String) -> Unit
) : RecyclerView.Adapter<ProductFiltersListAdapter.ProductFiltersViewHolder>() {
    private var selectedItems:Set<String> = emptySet()

    //establecer items seleccionados
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItems(selected: Set<String>) {
        selectedItems = selected
        notifyDataSetChanged()
    }

    inner class ProductFiltersViewHolder(private val binding: ItemFilterCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductFilterItem, checkStatus: Boolean,onToggle: (String)-> Unit) {
            binding.checkboxFilter.apply {
                text = item.name
                setOnCheckedChangeListener(null)
                isChecked = checkStatus
                setOnCheckedChangeListener { _, _->onToggle(item.name) }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductFiltersViewHolder {
        val binding =
            ItemFilterCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductFiltersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductFiltersViewHolder, position: Int) {
        val item = items[position]
        val isChecked = selectedItems.contains(item.name)
        holder.bind(item,isChecked,onToggle)
    }

    override fun getItemCount(): Int = items.size



}