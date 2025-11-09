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
    //buscar en local
    fun searchProducts(search:String,order:String?,sort:String?): ListProductsModel?{
        //validar si tiene todos los productos en local
        if (products == null || products?.total != products?.data?.size) return null
        val localProducts = products!!.data
        val terms = search.lowercase().split(" ").filter { it.isNotBlank() }
        //filtrar los productos
        val filtered = localProducts.filter { product ->
            terms.any { term ->
                product.name.lowercase().contains(term) ||
                        product.model.lowercase().contains(term) ||
                        product.brand.lowercase().contains(term) ||
                        product.processor.brand.lowercase().contains(term) ||
                        product.display.brand.lowercase().contains(term)
            }
        }.toMutableList()
        //ordenar
        val sortAndOrder = "${(order?.uppercase() ?: "REVIEW")}_${(sort?.uppercase() ?: "DESC")}"
        when (sortAndOrder) {
            "PRICE_ASC" -> filtered.sortBy { it.price }
            "PRICE_DESC" -> filtered.sortByDescending { it.price }
            "REVIEW_ASC" -> filtered.sortBy { it.qualification }
            "REVIEW_DESC" -> filtered.sortByDescending { it.qualification }
        }
        return ListProductsModel(
            total = filtered.size,
            pages = 1,
            first = 1,
            next = null,
            prev = null,
            data = filtered
        )
    }

    //limpiar productos
    fun clear() {
        products = null
    }
}