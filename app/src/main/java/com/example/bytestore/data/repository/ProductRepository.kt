package com.example.bytestore.data.repository

import android.util.Log
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.ProductFilters
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.ProductProvider
import com.example.bytestore.data.model.product.ProductProvider.isDifferentFrom
import com.example.bytestore.data.model.product.ProductRegisterRequest
import com.example.bytestore.data.network.product.ProductService
import com.example.bytestore.utils.Resource

class ProductRepository {
    private val productService by lazy { ProductService() }  //Obtengo el servicio de productos

    //obtener productos
    suspend fun getProducts(
        page: Int?, search: String?, sort: String?, order: String?
    ): ListProductsModel? {
        val limit = 16
        val cached = ProductProvider.getFetchProducts()
        val refreshInterval = 30 * 60 * 1000L //30 min
        var resetProducts = false
        //==============================
        //      busqueda en local
        //==============================
        if (cached != null && search != null) {
            val localSearch = ProductProvider.searchProducts(search, order, sort)
            //si la busqueda en local se puede realizar la retorno, en caso contrario sigue la solicitud normal
            if (localSearch != null) return localSearch
        }
        //==============================
        //      verificar cache de paginación
        //==============================
        // si ya solicte la pagina antes o si ya solicte todas las paginas
        if (cached != null && page != null && (cached.prev != null && page <= cached.prev || cached.next == null)) {
            //validar si se necesita actulizar
            if (!ProductProvider.needRefreshProducts(refreshInterval)) {
                Log.d("ProductRepository","Productos de cache: $page ")
                return cached
            }
            // si ya tengo todas las paginas las borro para solicitar los productos de nuevo
            if (cached.next == null) {
                ProductProvider.clearCachedProducts()
                resetProducts = true
            }
        }

        //lanzo la peticion y obtengo los datos
        val response = if (resetProducts) {
            productService.getProducts(
                1, limit, null, null, null
            )
        } else {
            productService.getProducts(
                page = page, limit = limit, search = search, sort = sort, order = order
            )
        }

        //Almaceno en el provider
        if (response != null) {
            ProductProvider.setProducts(response)
        }
        return response
    }

    //obtener un producto
    suspend fun getProduct(id: Int): ProductModel? {
        //obtengo el producto desde cache si existe
        val cachedProduct = ProductProvider.findProductById(id)
        if (cachedProduct != null) return cachedProduct
        //petición
        val response: ProductModel? = productService.getProduct(id)
        if (response != null) {
            ProductProvider.addProduct(response)
        }
        return response
    }

    //obtener productos similares
    suspend fun getSimilarProducts(id: Int): List<ProductModel>? {
       val cachedProducts = ProductProvider.getFetchProducts()
        if (cachedProducts !== null && cachedProducts.next == null) {
            return ProductProvider.getSimilarProducts(id)
        }
        val response: List<ProductModel>? = productService.getSimilarProducts(id)
        //agregar a cache
        response?.map { ProductProvider.addProduct(it) }
        return response
    }

    //obtener filtros
    suspend fun getProductFilters(): ProductFilters {
        val cache = ProductProvider.getFilters()

        //validar cache
        if (cache == null) {
            val response = productService.getProductFilters()!!
            ProductProvider.setFilters(response)
            return response
        }

        val refreshInterval = 30 * 60 * 1000L //30 minutos
        //validar si no necesita actualizarse
        if (!ProductProvider.needRefreshFilters(refreshInterval)) {
            return cache
        }

        val remote = productService.getProductFilters()!!

        //Compara hay cambio entre los datos
        return if (remote.isDifferentFrom(cache)) {
            ProductProvider.setFilters(remote)
            remote
        } else {
            ProductProvider.setFilters(cache)
            cache
        }
    }

    //obtener lista de productos por id
    suspend fun getProductsByIds(ids: List<Int>): Resource<List<ProductModel>> {
        val cachedProducts = ProductProvider.getProductsByIds(ids)
        if (!cachedProducts.isEmpty()) return Resource.Success(cachedProducts)
        //peticion
        val response = productService.getProductsByIds(ids.joinToString(","))
        //cache
        if (response is Resource.Success) {
            ProductProvider.addProducts(response.data)
        }
        return response
    }

    //registrar un producto
    suspend fun registerProduct(request: ProductRegisterRequest): Resource<ProductModel>{
        val response = productService.registerProduct(request)
        if(response is Resource.Success){
            ProductProvider.addProduct(response.data)
        }
        return response
    }
}