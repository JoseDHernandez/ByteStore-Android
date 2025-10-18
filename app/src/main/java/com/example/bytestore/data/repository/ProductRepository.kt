package com.example.bytestore.data.repository

import com.example.bytestore.data.model.product.ListProductsModels
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.ProductProvider
import com.example.bytestore.data.network.Product.ProductService

class ProductRepository {
    private val Api = ProductService() //Obtengo el servicio de productos
    suspend fun getProducts(
        page: Int?,
        limit: Int?,
        search: String?,
        sort: String?,
        order: String?
    ): ListProductsModels? {
        //verifico cache
        ProductProvider.getFetchProducts()?.let { cached ->
            if (cached.next == ((page ?: 1) + 1) || cached.next == page) {
                return cached
            }
        }
        //lanzo la peticion y obtengo los datos
        val response: ListProductsModels? = Api.getProducts(
            page = page,
            limit = limit,
            search = search,
            sort = sort,
            order = order
        )
        //Almaceno en el provider (almacenamiento local: cache)
        if (response != null) {
            ProductProvider.products = response
        }
        return response //retorno los datos del body
    }
}