package com.example.bytestore.ui.admin.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bytestore.data.model.user.UserModel
import com.example.bytestore.databinding.ItemUserCardBinding

class AdminUsersAdapter(private val onItemClick: (UserModel) -> Unit) :
    ListAdapter<UserModel, AdminUsersAdapter.UserViewHolder>(UserDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            ItemUserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserModel) {
            //datos
            binding.userId.text = user.id
            binding.userEmail.text = user.email
            binding.userName.text = user.name
            binding.tag.visibility =
                if (user.role.uppercase() == "ADMINISTRADOR") View.VISIBLE else View.INVISIBLE
            //seleccion
            binding.root.setOnClickListener {
                onItemClick(user)
            }

        }
    }

    private class UserDiff : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }
    }
}