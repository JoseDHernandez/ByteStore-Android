package com.example.bytestore.data.network.Product

import com.example.bytestore.data.model.product.ListProductsModels
import com.example.bytestore.data.model.product.ProductModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {
    @GET("/products")
    suspend fun getProducts(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("search") search: String?,
        @Query("sort") sort: String?,
        @Query("order") order: String?
    ): Response<ListProductsModels>
}