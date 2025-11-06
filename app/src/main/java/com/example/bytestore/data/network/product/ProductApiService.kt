package com.example.bytestore.data.network.product

import android.R
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.ProductFilters
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
    //obtener productos similares
    @GET("/products/{id}/similar")
    suspend fun getSimilarProducts(@Path("id")id: Int): Response<List<ProductModel>>
    //filtros
    @GET("/products/filters")
    suspend fun getProductFilters(): Response<ProductFilters>
}