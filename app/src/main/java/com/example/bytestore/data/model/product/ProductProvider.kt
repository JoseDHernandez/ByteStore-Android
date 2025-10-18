package com.example.bytestore.data.model.product

import androidx.collection.emptyObjectList

object ProductProvider {
    var products: ListProductsModels? = null

    //retornar los productos almacenados
    fun getFetchProducts(): ListProductsModels?{
        return products
    }

    //buscar en los productos almacenados si existe el producto necsitado
    fun findProductById(id:Int): ProductModel?{
        return products?.data?.find { it.id==id }
    }

    fun clear() {
        products =null
    }
}