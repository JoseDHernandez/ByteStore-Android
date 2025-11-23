package com.example.bytestore.data.model.cart

import com.example.bytestore.utils.centsToMoney
import kotlin.math.roundToLong

data class ListCartItemsModel(
    val total: Int,
    val pages: Int,
    val first: Int,
    val next: Int? = null,
    val prev: Int? = null,
    val data: List<CartItemModel> = emptyList()
)

/**
 * Representa una línea del carrito (un producto con cantidad).
 * Mantiene campos simples como en ProductModel.
 */
data class CartItemModel(
    val id: Long,              // id de la línea de carrito (no del producto)
    val productId: Int,        // id del producto referenciado
    val name: String,
    val image: String? = null, // url imagen
    val unitPrice: Float,
    val quantity: Int,
    val userId: Long? = null,
    val synced: Boolean = false,
    val updatedAt: Long        // epoch millis
) {
    val lineTotal: Float get() = unitPrice * quantity

    // Mostrar precio sin decimales para consistencia con la UI (pesos enteros)
    val formattedPrice: String
        get() = centsToMoney(
            (unitPrice * 100).roundToLong(),
            showCents = false
        )
}
