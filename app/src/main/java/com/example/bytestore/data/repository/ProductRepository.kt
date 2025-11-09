package com.example.bytestore.data.repository

import android.util.Log
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.ProductFilters
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.ProductProvider
import com.example.bytestore.data.network.product.ProductService

class ProductRepository {
    private val productService by lazy { ProductService() }  //Obtengo el servicio de productos

    //obtener productos
    suspend fun getProducts(
        page: Int?, limit: Int?, search: String?, sort: String?, order: String?
    ): ListProductsModel? {
        val cached = ProductProvider.getFetchProducts()
        //==============================
        //      busqueda en local
        //==============================
        if(cached !=null && search != null) {
            val localSearch = ProductProvider.searchProducts(search,order,sort)
            //si la busqueda en local se puede realizar la retorno, en caso contrario sigue la solicitud normal
            if(localSearch!=null) return localSearch
        }
        //==============================
        //      verificar cache de paginación
        //==============================
        // si ya solicte la pagina antes o si ya solicte todas las paginas
        if (cached != null && page != null && (cached.prev != null && page <= cached.prev || cached.next == null)) {
            Log.d("ProductRepository", "Retorno de cache")
            return cached
        }
        //lanzo la peticion y obtengo los datos
        val response: ListProductsModel? = productService.getProducts(
            page = page, limit = limit, search = search, sort = sort, order = order
        )
        //Almaceno en el provider (almacenamiento local: cache)
        if (response != null) {
            //validar cache
            if(cached != null){
                //agregar productos que no esten en local
                val newProducts = response.data.filter { newItem ->
                    cached.data.none { it.id == newItem.id }
                }
                if (newProducts.isNotEmpty()) {
                    val updatedCache = cached.copy(
                        data = cached.data + newProducts,
                        total = cached.total,
                        pages = cached.pages,
                        next = cached.next,
                        prev = cached.prev
                    )
                    ProductProvider.products = updatedCache
                }
                return response
            } else {
                ProductProvider.products = response
              return response
            }
        }
        return  null
    }

    //obtener un producto
    suspend fun getProduct(id: Int): ProductModel? {
        //obtengo el producto desde cache si existe
        val cachedProduct = ProductProvider.findProductById(id)
        if (cachedProduct != null) return cachedProduct
        //petición
        val response: ProductModel? = productService.getProduct(id.toString())
        return response
    }

    //obtener productos similares
    suspend fun getSimilarProducts(id: Int): List<ProductModel>? {
        //TODO: pendiente cache
        val response: List<ProductModel>? = productService.getSimilarProducts(id)
        return response
    }
    //obtener filtros
    suspend fun getProductFilters(): ProductFilters?{
        //TODO: pendiente cache
        val response: ProductFilters?= productService.getProductFilters()
        return response
    }
}