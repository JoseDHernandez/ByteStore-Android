package com.example.bytestore.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_items",
    indices = [Index(value = ["productId"], unique = true)]
)
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val name: String,
    val imageUrl: String?,
    val unitPriceCents: Long,
    val quantity: Int,
    val userId: Long? = null,
    val synced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
