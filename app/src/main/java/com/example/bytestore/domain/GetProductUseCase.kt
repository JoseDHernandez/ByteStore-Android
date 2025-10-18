package com.example.bytestore.domain

import com.example.bytestore.data.model.product.ListProductsModels
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.repository.ProductRepository

class GetProductUseCase {
    private val repository= ProductRepository()

    suspend operator fun invoke(): ListProductsModels?{
        return repository.getProducts(page = null, limit = null, search = null,sort = null,order = null)
    }
}