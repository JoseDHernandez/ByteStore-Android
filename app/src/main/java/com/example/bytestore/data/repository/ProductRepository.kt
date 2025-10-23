package com.example.bytestore.data.repository

import android.util.Log
import com.example.bytestore.data.model.product.ListProductsModels
import com.example.bytestore.data.model.product.ProductProvider
import com.example.bytestore.data.network.product.ProductService

class ProductRepository {
    private val Api = ProductService() //Obtengo el servicio de productos
    suspend fun getProducts(
        page: Int?, limit: Int?, search: String?, sort: String?, order: String?
    ): ListProductsModels? {
        val cached = ProductProvider.getFetchProducts()
        //verifico cache y que sea la misma pagina
        if (cached != null && page != null //cached y page no nulas
            && (
                    cached.prev != null && page <= cached.prev // si ya solicte la pagina antes
                            || cached.next == null // si ya sokicte todas las paginas
                    )
        ) {
            Log.d("ProductRepository", "Retorno de cache")
            return cached
        }
        //lanzo la peticion y obtengo los datos
        val response: ListProductsModels? = Api.getProducts(
            page = page, limit = limit, search = search, sort = sort, order = order
        )
        //Almaceno en el provider (almacenamiento local: cache)
        if (response != null) {
            //validar si es una nueva pagina
            val updateList = if (cached != null && page != 1) {
                //almacenar en cache los datos de la nueva pagina
                Log.d("ProductRepocitory", "prev: ${response.prev}, next: ${response.next}")
                cached.copy(
                    data = cached.data + response.data,
                    total = response.total,
                    pages = response.pages,
                    next = response.next,
                    prev = response.prev
                )
            } else {
                response
            }
            ProductProvider.products = updateList
            return updateList
        }
        return response //retorno los datos del body
    }
}