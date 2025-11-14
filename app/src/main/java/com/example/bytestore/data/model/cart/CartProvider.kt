package com.example.bytestore.data.model.cart

object CartProvider {
    var cart: ListCartItemsModel? = null

    // Retorna el carrito almacenado en memoria (misma idea de ProductProvider)
    fun getFetchCart(): ListCartItemsModel? = cart

    // Encuentra una línea por productId (útil para “añadir al carrito”)
    fun findItemByProductId(productId: Int): CartItemModel? =
        cart?.data?.find { it.productId == productId }

    // Cantidad total de ítems (sumando cantidades)
    fun size(): Int = cart?.data?.sumOf { it.quantity } ?: 0

    // Total a pagar
    fun totalAmount(): Float =
        cart?.data?.fold(0f) { acc, item -> acc + item.lineTotal } ?: 0f

    // Limpia el carrito en memoria
    fun clear() { cart = null }

    // Inserta/actualiza una línea en memoria (no DB). Mantiene el wrapper como ProductProvider.
    fun upsert(item: CartItemModel) {
        val current = cart?.data?.toMutableList() ?: mutableListOf()
        val idx = current.indexOfFirst { it.productId == item.productId }
        if (idx >= 0) {
            // conserva el id de la línea existente
            current[idx] = item.copy(id = current[idx].id)
        } else {
            current.add(item)
        }
        cart = ListCartItemsModel(
            total = current.size,
            pages = 1,
            first = 1,
            data = current
        )
    }
}
