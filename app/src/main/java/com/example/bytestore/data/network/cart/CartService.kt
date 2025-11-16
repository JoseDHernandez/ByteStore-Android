package com.example.bytestore.data.network.cart

import android.content.Context
import android.util.Log
import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.cart.CartItemModel
import com.example.bytestore.data.model.cart.ListCartItemsModel
import com.example.bytestore.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.bytestore.core.DBProvider
import com.example.bytestore.data.local.CartEntity

/**
 * CartService adaptado al API-Gateway: usa /carts?user_id=... y POST/PUT /carts
 */
class CartService(private val context: Context) {
    private val api = ApiClient.retrofit(context).create(CartApiService::class.java)
    private val session = SessionManager(context)

    companion object {
        private const val TAG = "CartService"
    }

    // Intenta obtener el carrito del usuario actual (por user_id)
    suspend fun getCart(): ListCartItemsModel? = withContext(Dispatchers.IO) {
        val user = runCatching { session.getCurrentUser() }.getOrNull()

        if (user == null ) {
            Log.e(TAG, "No se pudo obtener el usuario de la sesión")
            return@withContext null
        }

        Log.d(TAG, "GET Cart - User ID: ${user.id}")
        Log.d(TAG, "GET Cart - User Name: ${user.name}")
        Log.d(TAG, "GET Cart - User Email: ${user.email}")

        val resp = runCatching { api.getByUser(user.id) }.getOrNull()

        if (resp == null) {
            Log.e(TAG, "Error en la petición GET /carts")
            return@withContext null
        }

        Log.d(TAG, "GET Cart Response - Success: ${resp.isSuccessful}, Code: ${resp.code()}")

        return@withContext if (resp.isSuccessful) {
            val body = resp.body()
            Log.d(TAG, "GET Cart Response Body - Items: ${body?.items?.size ?: 0}")
            body?.toModel()
        } else {
            Log.e(TAG, "GET Cart Failed - Error: ${resp.errorBody()?.string()}")
            null
        }
    }

    // Agrega o crea el carrito en el servidor: GET by user -> POST (si no existe) o PUT (si existe)
    suspend fun addItem(productId: Long, qty: Int, unitPriceCents: Long): ListCartItemsModel? = withContext(Dispatchers.IO) {
        val user = runCatching { session.getCurrentUser() }.getOrNull()

        if (user == null) {
            Log.e(TAG, "ADD - No se pudo obtener el usuario de la sesión")
            return@withContext null
        }

        Log.d(TAG, "ADD Item - User ID: ${user.id}")
        Log.d(TAG, "ADD Item - Product ID: $productId, Qty: $qty, Price: $unitPriceCents centavos")

        val current = runCatching { api.getByUser(user.id) }.getOrNull()

        if (current == null || !current.isSuccessful || current.body() == null) {
            // No existe carrito: crear
            Log.d(TAG, "Creando nuevo carrito para user: ${user.id}")

            val body = CreateCartRequest(
                userId = user.id,
                items = listOf(CreateCartItemDto(productId, qty, unitPriceCents))
            )

            Log.d(TAG, "POST /carts - userId: ${body.userId}, items: ${body.items.size}")

            val createResp = runCatching { api.createCart(body) }.getOrNull()

            if (createResp != null) {
                Log.d(TAG, "POST Response - Success: ${createResp.isSuccessful}, Code: ${createResp.code()}")

                if (createResp.isSuccessful) {
                    val responseBody = createResp.body()
                    Log.d(TAG, "POST Response - Cart ID: ${responseBody?.id}")
                    Log.d(TAG, "POST Response - Items: ${responseBody?.items?.size}")

                    // Si el servidor devuelve null o vacío, crear modelo local
                    if (responseBody == null || responseBody.items.isNullOrEmpty()) {
                        Log.w(TAG, "Servidor devolvió respuesta vacía, usando datos locales")

                        // Intentar rellenar nombre/imagen desde la BD local si existe
                        try {
                            val dao = DBProvider.getCartDao(context)
                            val local = dao.findByProduct(productId)
                            val name = local?.name ?: ""
                            val image = local?.imageUrl

                            return@withContext ListCartItemsModel(
                                total = 1,
                                pages = 1,
                                first = 1,
                                data = listOf(
                                    CartItemModel(
                                        id = 0,
                                        productId = productId.toInt(),
                                        name = name,
                                        image = image,
                                        unitPrice = (unitPriceCents / 100f),
                                        quantity = qty,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "No se pudo leer localmente: ${e.message}")
                            return@withContext ListCartItemsModel(
                                total = 1,
                                pages = 1,
                                first = 1,
                                data = listOf(
                                    CartItemModel(
                                        id = 0,
                                        productId = productId.toInt(),
                                        name = "",
                                        image = null,
                                        unitPrice = (unitPriceCents / 100f),
                                        quantity = qty,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            )
                        }
                    }

                    return@withContext responseBody.toModel()
                } else {
                    Log.e(TAG, "POST Failed - Error: ${createResp.errorBody()?.string()}")
                }
            } else {
                Log.e(TAG, "POST Request Failed")
            }

            return@withContext null
        } else {
            // Existe carrito: combinar items y reemplazar
            Log.d(TAG, "📝 Actualizando carrito existente")

            val cartDto = current.body()!!
            val existing = cartDto.items.toMutableList()
            val idx = existing.indexOfFirst { it.productId == productId }

            if (idx >= 0) {
                val item = existing[idx]
                existing[idx] = item.copy(quantity = item.quantity + qty)
                Log.d(TAG, "📝 Item existente actualizado - Nueva qty: ${item.quantity + qty}")
            } else {
                existing.add(CartItemDto(productId, "", null, unitPriceCents, qty))
                Log.d(TAG, "📝 Nuevo item agregado al carrito")
            }

            val itemsPayload = existing.map { CreateCartItemDto(it.productId, it.quantity, it.unitPrice) }
            val cartId = cartDto.id ?: -1L

            if (cartId <= 0) {
                // Fallback: crear
                Log.w(TAG, "Cart ID inválido, creando nuevo carrito")
                val body = CreateCartRequest(user.id, itemsPayload)
                runCatching { api.createCart(body) }.getOrNull()?.takeIf { it.isSuccessful }?.body()?.toModel()
            } else {
                Log.d(TAG, "📤 PUT /carts/$cartId - Items: ${itemsPayload.size}")
                val body = ReplaceCartRequest(itemsPayload)
                val putResp = runCatching { api.replaceCart(cartId, body) }.getOrNull()

                if (putResp != null) {
                    Log.d(TAG, "PUT Response - Success: ${putResp.isSuccessful}, Code: ${putResp.code()}")

                    if (putResp.isSuccessful) {
                        return@withContext putResp.body()?.toModel()
                    } else {
                        Log.e(TAG, "PUT Failed - Error: ${putResp.errorBody()?.string()}")
                    }
                }

                return@withContext null
            }
        }
    }

    suspend fun updateQty(productId: Long, qty: Int): ListCartItemsModel? = withContext(Dispatchers.IO) {
        val user = runCatching { session.getCurrentUser() }.getOrNull() ?: return@withContext null
        val current = runCatching { api.getByUser(user.id) }.getOrNull() ?: return@withContext null
        val cartDto = current.body() ?: return@withContext null
        val existing = cartDto.items.map { if (it.productId == productId) it.copy(quantity = qty) else it }
        val itemsPayload = existing.map { CreateCartItemDto(it.productId, it.quantity, it.unitPrice) }
        val cartId = cartDto.id ?: return@withContext null
        runCatching { api.replaceCart(cartId, ReplaceCartRequest(itemsPayload)) }.getOrNull()
            ?.takeIf { it.isSuccessful }?.body()?.toModel()
    }

    suspend fun removeItem(productId: Long): ListCartItemsModel? = withContext(Dispatchers.IO) {
        val user = runCatching { session.getCurrentUser() }.getOrNull() ?: return@withContext null
        val current = runCatching { api.getByUser(user.id) }.getOrNull() ?: return@withContext null
        val cartDto = current.body() ?: return@withContext null
        val remaining = cartDto.items.filter { it.productId != productId }
        val itemsPayload = remaining.map { CreateCartItemDto(it.productId, it.quantity, it.unitPrice) }
        val cartId = cartDto.id ?: return@withContext null
        runCatching { api.replaceCart(cartId, ReplaceCartRequest(itemsPayload)) }.getOrNull()
            ?.takeIf { it.isSuccessful }?.body()?.toModel()
    }

    suspend fun clear(): Boolean = withContext(Dispatchers.IO) {
        val user = runCatching { session.getCurrentUser() }.getOrNull() ?: return@withContext false
        val current = runCatching { api.getByUser(user.id) }.getOrNull() ?: return@withContext false
        val cartId = current.body()?.id ?: return@withContext false
        runCatching { api.deleteCart(cartId) }.getOrNull()?.isSuccessful == true
    }

    /**
     * Crea un carrito en el servidor usando el payload de items proporcionado.
     * Retorna el modelo convertido si la operación fue exitosa.
     */
    suspend fun createCartFromPayload(items: List<CreateCartItemDto>): ListCartItemsModel? = withContext(Dispatchers.IO) {
        val user = runCatching { session.getCurrentUser() }.getOrNull() ?: return@withContext null
        val body = CreateCartRequest(userId = user.id, items = items)
        runCatching { api.createCart(body) }.getOrNull()?.takeIf { it.isSuccessful }?.body()?.toModel()
    }
}

private fun CartDto.toModel(): ListCartItemsModel {
    val safeItems = items ?: emptyList()
    return ListCartItemsModel(
        total = safeItems.size,
        pages = 1,
        first = 1,
        data = safeItems.map { it.toModel() }
    )
}

private fun CartItemDto.toModel() = CartItemModel(
    id = 0,
    productId = productId.toInt(),
    name = name,
    image = imageUrl,
    unitPrice = ((unitPrice ?: 0L) / 100f),
    quantity = quantity,
    updatedAt = System.currentTimeMillis()
)