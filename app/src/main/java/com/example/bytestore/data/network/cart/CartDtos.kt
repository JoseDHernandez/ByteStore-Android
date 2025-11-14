package com.example.bytestore.data.network.cart

import com.google.gson.annotations.SerializedName

/**
 * DTOs alineados con el servicio de carrito detrás del API Gateway (/carts)
 */
data class CartDto(
    val id: Long? = null,
    val items: List<CartItemDto>,
    val updatedAt: String
)

data class CartItemDto(
    @SerializedName("product_id") val productId: Long,
    val name: String,
    val imageUrl: String?,
    @SerializedName("unit_price") val unitPrice: Long, // centavos
    val quantity: Int
)

data class CreateCartItemDto(
    @SerializedName("product_id") val productId: Long,
    val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Long? = null
)

data class CreateCartRequest(
    @SerializedName("user_id") val userId: String,
    val items: List<CreateCartItemDto>
)

data class ReplaceCartRequest(
    val items: List<CreateCartItemDto>
)
