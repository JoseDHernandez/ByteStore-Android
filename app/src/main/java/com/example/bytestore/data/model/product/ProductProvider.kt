package com.example.bytestore.data.model.product

object ProductProvider {
    var products: ListProductsModel? = null

    //retornar los productos almacenados
    fun getFetchProducts(): ListProductsModel? {
        return products
    }

    //buscar en los productos almacenados si existe el producto necsitado
    fun findProductById(id: Int): ProductModel? {
        return products?.data?.find { it.id == id }
    }

    fun clear() {
        products = null
    }
}