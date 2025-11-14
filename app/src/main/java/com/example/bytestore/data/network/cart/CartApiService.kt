package com.example.bytestore.data.network.cart

import retrofit2.Response
import retrofit2.http.*

/**
 * API definitions mapped to the API-Gateway exposed routes under /carts
 */
interface CartApiService {
    // Obtiene el carrito del usuario: /carts?user_id=<uuid>
    @GET("carts")
    suspend fun getByUser(@Query("user_id") userId: String): Response<CartDto>

    // Crea un carrito (POST /carts)
    @POST("carts")
    suspend fun createCart(@Body body: CreateCartRequest): Response<CartDto>

    // Reemplaza/actualiza el carrito completo (PUT /carts/:id)
    @PUT("carts/{id}")
    suspend fun replaceCart(@Path("id") id: Long, @Body body: ReplaceCartRequest): Response<CartDto>

    // Borra carrito por id (DELETE /carts/:id)
    @DELETE("carts/{id}")
    suspend fun deleteCart(@Path("id") id: Long): Response<Unit>
}
