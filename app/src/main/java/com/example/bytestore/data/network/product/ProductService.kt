package com.example.bytestore.data.network.product

import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ProductService {
    private val api = ApiClient.retrofit() //Solicito el helper de retrofit

    // ===== Nota ====
    // getProducts y getProduct no retornar Resourse<ListProductsModel>
    // o Resource<ProductModel>, ya que los caso de error son mayormente
    // 404 0 500 y estos se pueden manejar con null. Para peticiones con
    // codigos mas explicitos usar Resource.
    // ===============

    suspend fun getProducts(
        page: Int?,
        limit: Int?,
        search: String?,
        sort: String?,
        order: String?
    ): ListProductsModel? = withContext(Dispatchers.IO) {
        //capto la respuesta en el data model/ dto
        try {
            val response: Response<ListProductsModel> =
                //petición basada en la establecida en el ApiModel
                api.create(ProductApiService::class.java).getProducts(
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

    suspend fun getProduct(id:String): ProductModel? = withContext(Dispatchers.IO) {
        try {
            val response: Response<ProductModel> = api.create(ProductApiService::class.java).getProduct(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}