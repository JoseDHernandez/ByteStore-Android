package com.example.bytestore.ui.admin.product.processor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bytestore.data.model.product.ProcessorModel
import com.example.bytestore.databinding.ItemProcessorCardBinding

class ProcessorAdapter(private val onItemClick: (ProcessorModel)->Unit): ListAdapter<ProcessorModel, ProcessorAdapter.ProcessorViewHolder>(
    ProcessorDiff()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessorViewHolder {
        val binding = ItemProcessorCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ProcessorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProcessorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class ProcessorViewHolder(private val binding: ItemProcessorCardBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(processor: ProcessorModel){
            binding.processorInfo.text = "${processor.family} - ${processor.model}"
            binding.processorBrand.text = processor.brand
            binding.root.setOnClickListener {
                onItemClick(processor)
            }
        }
    }
    private class ProcessorDiff: DiffUtil.ItemCallback<ProcessorModel>(){
        override fun areItemsTheSame(oldItem: ProcessorModel, newItem: ProcessorModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProcessorModel, newItem: ProcessorModel): Boolean {
            return oldItem == newItem
        }
    }
}