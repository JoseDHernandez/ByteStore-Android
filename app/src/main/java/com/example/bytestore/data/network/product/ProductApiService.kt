package com.example.bytestore.data.network.product

import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.ProductModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    //obtener productos
    @GET("/products")
    suspend fun getProducts(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("search") search: String?,
        @Query("sort") sort: String?,
        @Query("order") order: String?
    ): Response<ListProductsModel>
    //obtener un solo producto por id
    @GET("/products/{id}")
    suspend fun getProduct(@Path("id") id:String): Response<ProductModel>
}