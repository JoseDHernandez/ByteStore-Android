package com.example.bytestore.data.repository

import android.util.Log
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.ProductProvider
import com.example.bytestore.data.network.product.ProductService

class ProductRepository {
    private val Api = ProductService() //Obtengo el servicio de productos

    //obtener productos
    suspend fun getProducts(
        page: Int?, limit: Int?, search: String?, sort: String?, order: String?
    ): ListProductsModel? {
        val cached = ProductProvider.getFetchProducts()
        //verifico cache y que sea la misma pagina
        // si ya solicte la pagina antes o si ya solicte todas las paginas
        if (cached != null && page != null && (cached.prev != null && page <= cached.prev || cached.next == null)) {
            Log.d("ProductRepository", "Retorno de cache")
            return cached
        }
        //lanzo la peticion y obtengo los datos
        val response: ListProductsModel? = Api.getProducts(
            page = page, limit = limit, search = search, sort = sort, order = order
        )
        //Almaceno en el provider (almacenamiento local: cache)
        if (response != null) {
            //validar si es una nueva pagina
            val updateList = if (cached != null && page != 1) {
                //almacenar en cache los datos de la nueva pagina
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

    //obtener un producto
    suspend fun getProduct(id: Int): ProductModel? {
        //obtengo el producto desde cache si existe
        val cachedProduct = ProductProvider.findProductById(id)
        if (cachedProduct != null) return cachedProduct
        //petición
        val response: ProductModel? = Api.getProduct(id.toString())
        return response
    }
}