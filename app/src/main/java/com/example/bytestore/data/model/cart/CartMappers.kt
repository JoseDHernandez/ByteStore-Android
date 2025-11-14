package com.example.bytestore.data.model.cart

import com.example.bytestore.data.local.CartEntity

// Si tu clase es CartItemModel, deja así.
// Si se llama CartItem, cambia CartItemModel -> CartItem en ambas funciones.
fun CartEntity.toModel(): CartItemModel =
    CartItemModel(
        id = id,
        productId = productId.toInt(),
        name = name,
        image = imageUrl,
        unitPrice = unitPriceCents / 100f,
        quantity = quantity,
        userId = userId,
        synced = synced,
        updatedAt = updatedAt
    )

fun CartItemModel.toEntity(): CartEntity =
    CartEntity(
        id = id,
        productId = productId.toLong(),
        name = name,
        imageUrl = image,
        unitPriceCents = (unitPrice * 100).toLong(),
        quantity = quantity,
        userId = userId,
        synced = synced,
        updatedAt = updatedAt
    )
