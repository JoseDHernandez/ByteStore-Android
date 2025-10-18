package com.example.bytestore.data.network.Product

import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.product.ListProductsModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ProductService {
    private val Api = ApiClient.retrofit() //Solicito el helper de retrofit

    suspend fun getProducts(
        page: Int?,
        limit: Int?,
        search: String?,
        sort: String?,
        order: String?
    ): ListProductsModels? = withContext(Dispatchers.IO) {
        //capto la respuesta en el data model/ dto
        try {
            val response: Response<ListProductsModels> =
                //petición basada en la establecida en el ApiModel
                Api.create(ProductApiService::class.java).getProducts(
                    page = page,
                    limit = limit,
                    search = search,
                    sort = sort,
                    order = order
                )
            if (response.isSuccessful) response.body() else null //cuerpo de la respuesta
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}