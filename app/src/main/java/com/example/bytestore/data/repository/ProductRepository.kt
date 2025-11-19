package com.example.bytestore.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.bytestore.data.model.product.BrandModel
import com.example.bytestore.data.model.product.DisplayModel
import com.example.bytestore.data.model.product.DisplayRegisterRequest
import com.example.bytestore.data.model.product.DisplayUpdateRequest
import com.example.bytestore.data.model.product.ImagenResponseModel
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.OSRegisterRequest
import com.example.bytestore.data.model.product.OSUpdateRequest
import com.example.bytestore.data.model.product.OperatingSystemModel
import com.example.bytestore.data.model.product.ProcessorModel
import com.example.bytestore.data.model.product.ProcessorRegisterRequest
import com.example.bytestore.data.model.product.ProcessorUpdateRequest
import com.example.bytestore.data.model.product.ProductBrandRequest
import com.example.bytestore.data.model.product.ProductFilters
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.ProductProvider
import com.example.bytestore.data.model.product.ProductProvider.isDifferentFrom
import com.example.bytestore.data.model.product.ProductRegisterRequest
import com.example.bytestore.data.model.product.ProductUpdateRequest
import com.example.bytestore.data.network.product.ProductService
import com.example.bytestore.utils.ImageTools
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
    //actuzlizar un producto
    suspend fun updateProduct(id:Int,request: ProductUpdateRequest): Resource<ProductModel>{
        val response = productService.updateProduct(id,request)
        if(response is Resource.Success) {
            ProductProvider.addProduct(response.data)
        }
        return response
    }
    //remover producto (true = ok, false = error)
    suspend fun deleteProduct(id:Int): Boolean {
        return productService.deleteProduct(id)
    }

    //==========================================
    //             Imagenes
    //==========================================


    /**
     * **Subir imagen**
     *
     *  Adapta y envia la imagen al servidor
     *
     * @param context Contexto necesario para leer la imagen local.
     * @param uri URI de la imagen seleccionada (camara / galeria).
     * @return URL de la imagen subida en el servidor.
     */
    suspend fun uploadImage(context: Context, uri: Uri): Resource<String>{
        val multipart = ImageTools.adapterToWebp(context,uri)
        return productService.uploadImage(multipart)
    }

    /**
     * **Actulizar imagen**
     *
     *  Reemplaza una imagen existente en el servidor (conservando el nombre)
     *
     * @param context Contexto necesario para leer la imagen local.
     * @param uri URI de la imagen seleccionada (camara / galeria).
     * @param filename Nombre del archivo que se sobrescribirá en el servidor.
     * @return URL de la imagen reemplazada en el servidor.
     */
    suspend fun changeImage(context: Context,uri: Uri,filename:String): Resource<String> {
        val multipart = ImageTools.adapterToWebp(context,uri,filename)
        return productService.changeImage(filename,multipart)
    }
    /**
     *
     * **Eliminar imagen**
     *
     * Elimina una imagen almacenada en el servidor.
     * Usar cuando se elimina una imagen de forma individual o al borrar un producto completo.
     *
     * @param filename Nombre del archivo a eliminar (con extención del formato).
     * @return `true` si fue eliminada correctamente, `false` en caso contrario.
     */
    suspend fun deleteImage(filename: String): Boolean {
        return productService.deleteImage(filename)
    }

    //==========================================
    //             CRUD Marcas
    //==========================================

    //obtener marcas de los productos
    suspend fun getBrands(): Resource<List<BrandModel>> {
        //validar cache (ids no temporales)
        if(ProductProvider.checkAllBrandsIds()) return Resource.Success(ProductProvider.getBrands())
        val response= productService.getBrands()
        //sincronizar ids
        if(response is Resource.Success) {
            val brands = response.data
            brands.forEach { ProductProvider.syncBrandId(it) }
        }
        return response
    }
    //obtener marca por id
    suspend fun getBrand(id:Int): Resource<BrandModel> {
        val cachedBrand = ProductProvider.getBrand(id)
        if(ProductProvider.checkAllBrandsIds() && cachedBrand !=null) return Resource.Success(cachedBrand)
        return productService.getBrand(id)
    }
    //registrar
    suspend fun registerBrand(request: ProductBrandRequest): Resource<BrandModel> {
        val response = productService.registerBrand(request)
        if(response is Resource.Success){
            ProductProvider.addBrand(response.data)
        }
        return response
    }
    //eliminar marca
    suspend fun deleteBrand(id:Int): Boolean{
        val response = productService.deleteBrand(id)
        if(response) ProductProvider.removeBrand(id)
        return response
    }
    //actulizar marca
    suspend fun updateBrand(id:Int, request: ProductBrandRequest): Resource<BrandModel>{
        val response = productService.updateBrand(id,request)
        if(response is Resource.Success) ProductProvider.updateBrand(id,response.data)
        return response
    }

    //==========================================
    //             CRUD graficos
    //==========================================

    //obtener graficos
    suspend fun getDisplays(): Resource<List<DisplayModel>>{
        //validar cache
        if(ProductProvider.checkAllDisplaysIds()) return Resource.Success(ProductProvider.getDisplays())
        val response = productService.getDisplays()
        //sincronizar ids
        if(response is Resource.Success) response.data.forEach { ProductProvider.syncDisplayId(it) }
        return response
    }
    //obtener grafico
    suspend fun getDisplay(id:Int) : Resource<DisplayModel> {
        val cachedDisplay = ProductProvider.getDisplays(id)
        if(cachedDisplay!= null) return Resource.Success(cachedDisplay)
        return productService.getDisplay(id)
    }
    //registrar grafico
    suspend fun registerDisplay(request: DisplayRegisterRequest): Resource<DisplayModel> {
        val response = productService.registerDisplay(request)
        if(response is Resource.Success) ProductProvider.addDisplay(response.data)
        return response
    }
    //actulizar grafico
    suspend fun updateDisplay(id:Int, request: DisplayUpdateRequest): Resource<DisplayModel> {
        val response = productService.updateDisplay(id,request)
        if(response is Resource.Success) ProductProvider.updateDisplay(id,response.data)
        return response
    }
    //eliminar
    suspend fun deleteDisplay(id:Int): Boolean {
        val response = productService.deleteDisplay(id)
        if(response) ProductProvider.removeDisplay(id)
        return response
    }

    //==========================================
    //             CRUD procesadores
    //==========================================

    //obtener procesadores
    suspend fun getProcessors(): Resource<List<ProcessorModel>> {
        if(ProductProvider.checkAllProcessorsIds()) return Resource.Success(ProductProvider.getProcessors())
        val response = productService.getProcessors()
        if(response is Resource.Success) response.data.forEach { ProductProvider.syncProcessorId(it) }
        return response
    }
    //obtener procesador
    suspend fun getProcessor(id:Int): Resource<ProcessorModel> {
        val cachedProcessor = ProductProvider.getProcessor(id)
        if(cachedProcessor!=null)return Resource.Success(cachedProcessor)
        return productService.getProcessor(id)
    }
    //registrar procesador
    suspend fun registerProcessor(request: ProcessorRegisterRequest): Resource<ProcessorModel> {
        val response = productService.registerProcessor(request)
        if(response is Resource.Success) ProductProvider.addProcessor(response.data)
        return response
    }
    //actulizar
    suspend fun updateProcessor(id:Int,request: ProcessorUpdateRequest): Resource<ProcessorModel> {
        val response = productService.updateProcessor(id,request)
        if(response is Resource.Success) ProductProvider.updateProcessor(id,response.data)
        return response
    }
    //eliminar
    suspend fun deleteProcessor(id:Int): Boolean {
        val response = productService.deleteProcessor(id)
        if(response) ProductProvider.removeProcessor(id)
        return response
    }

    //==========================================
    //             CRUD sistemas operativos
    //==========================================

    suspend fun getOperatingSystems(): Resource<List<OperatingSystemModel>> {
        if(ProductProvider.checkAllOSIds()) return Resource.Success(ProductProvider.getOperatingSystems())
        val response = productService.getAllOS()
        if(response is Resource.Success) response.data.forEach { ProductProvider.syncOsId(it) }
        return response
    }

    suspend fun getOperatingSystem(id:Int): Resource<OperatingSystemModel> {
        val cachedOS = ProductProvider.getOperatingSystem(id)
        if(cachedOS!=null) return Resource.Success(cachedOS)
        return productService.getOS(id)
    }

    suspend fun registerOperatingSystem(request: OSRegisterRequest): Resource<OperatingSystemModel> {
        val response = productService.registerOS(request)
        if(response is Resource.Success) ProductProvider.addOS(response.data)
        return response
    }
    suspend fun updateOperatingSystem(id:Int, request: OSUpdateRequest): Resource<OperatingSystemModel> {
        val response = productService.updateOS(id,request)
        if(response is Resource.Success) ProductProvider.updateOS(id,response.data)
        return response
    }
    suspend fun deleteOperatingSystem(id:Int): Boolean {
        val response = productService.deleteOS(id)
        if(response) ProductProvider.removeOperatingSystem(id)
        return response
    }
}