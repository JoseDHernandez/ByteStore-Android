package com.example.bytestore.data.network.product

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
import com.example.bytestore.data.model.product.ProductRegisterRequest
import com.example.bytestore.data.model.product.ProductUpdateRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    //obtener productos
    @GET("/products")
    suspend fun getProducts(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("search") search: String?,
        @Query("sort") sort: String?,
        @Query("order") order: String?
    ): Response<ListProductsModel>

    //obtener un solo producto por id
    @GET("/products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<ProductModel>

    //obtener productos similares
    @GET("/products/{id}/similar")
    suspend fun getSimilarProducts(@Path("id") id: Int): Response<List<ProductModel>>

    //filtros
    @GET("/products/filters")
    suspend fun getProductFilters(): Response<ProductFilters>

    //Obtener productos de una lista
    @GET("/products")
    suspend fun getProductsByList(@Query("list") list: String): Response<List<ProductModel>>

    //Crear prodcuto
    @GET("/products/")
    suspend fun registerProduct(@Body request: ProductRegisterRequest): Response<ProductModel>

    //actualizar producto
    @PUT("/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: ProductUpdateRequest
    ): Response<ProductModel>

    //eliminar producto
    @DELETE("/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Any> //validar status code


    //=================================
    //             Imagenes
    //=================================

    //Subir imagen
    @Multipart
    @POST("/products/images/upload/")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ImagenResponseModel>

    //remplazar imagen
    @Multipart
    @PUT("/products/images/{filename}")
    suspend fun changeImage(
        @Path("filename") filename: String,
        @Part file: MultipartBody.Part
    ): Response<ImagenResponseModel>

    //eliminar iamgen
    @DELETE("/products/images/{filename}")
    suspend fun deleteImage(@Path("filename") filename: String): Response<Any>

    //=================================
    //             Marcas
    //=================================

    //todas las marcas
    @GET("/products/brands/")
    suspend fun getBrands(): Response<List<BrandModel>>

    //obtener marca por id
    @GET("/products/brands/{id}")
    suspend fun getBrandById(@Path("id") id: Int): Response<BrandModel>

    //registrar marca
    @POST("/products/brands/")
    suspend fun registerBrand(@Body request: ProductBrandRequest): Response<BrandModel>

    //actualizar marca
    @PATCH("/products/brands/{id}")
    suspend fun updateBrand(
        @Path("id") id: Int,
        @Body request: ProductBrandRequest
    ): Response<BrandModel>

    //eliminar marca
    @DELETE("/products/brands/{id}")
    suspend fun deleteBrand(@Path("id") id: Int): Response<Any>//validar status code

    //=================================
    //             Graficos/ displays
    //=================================

    //obtener pantallas
    @GET("/products/displays/")
    suspend fun getDisplays(): Response<List<DisplayModel>>

    //obtener por id
    @GET("/products/displays/{id}")
    suspend fun getDisplayById(@Path("id") id: Int): Response<DisplayModel>

    //registrar
    @POST("/products/displays/")
    suspend fun registerDisplay(@Body request: DisplayRegisterRequest): Response<DisplayModel>

    //actulizar
    @PUT("/products/displays/{id}")
    suspend fun updateDisplay(
        @Path("id") id: Int,
        @Body request: DisplayUpdateRequest
    ): Response<DisplayModel>

    //eliminar
    @DELETE("/products/displays/{id}")
    suspend fun deleteDisplay(@Path("id") id: Int): Response<Any>

    //=================================
    //            procesadores
    //=================================

    //obtener
    @GET("/products/processors/")
    suspend fun getProcessors(): Response<List<ProcessorModel>>

    //obtner por id
    @GET("/produtics/processors/{id}")
    suspend fun getProcessorById(@Path("id") id: Int): Response<ProcessorModel>

    //registrar
    @POST("/products/processors/")
    suspend fun registerProcessor(@Body request: ProcessorRegisterRequest): Response<ProcessorModel>

    //actualizar
    @PUT("/products/processors/{id}")
    suspend fun updateProcessor(
        @Path("id") id: Int,
        @Body request: ProcessorUpdateRequest
    ): Response<ProcessorModel>

    //eliminar
    @DELETE("/products/processors/{id}")
    suspend fun deleteProcessor(@Path("id") id: Int): Response<Any>

    //=================================
    //            sistemas operativos
    //=================================

    //obtener
    @GET("/products/systems/")
    suspend fun getOS(): Response<List<OperatingSystemModel>>

    //obtener por id
    @GET("/products/systems/{id}")
    suspend fun getOSById(@Path("id") id: Int): Response<OperatingSystemModel>

    //actualizar
    @PUT("/products/systems/{id}")
    suspend fun updateOS(
        @Path("id") id: Int,
        @Body request: OSUpdateRequest

    ): Response<OperatingSystemModel>

    //registrar
    @POST("/products/systems/")
    suspend fun registerOS(@Body request: OSRegisterRequest): Response<OperatingSystemModel>

    //eliminar
    @DELETE("/products/systems/{id}")
    suspend fun deleteOS(@Path("id") id: Int): Response<Any>


}