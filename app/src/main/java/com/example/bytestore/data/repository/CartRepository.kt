package com.example.bytestore.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.withTransaction
import com.example.bytestore.core.DBProvider
import com.example.bytestore.data.local.CartEntity
import com.example.bytestore.data.model.cart.CartItemModel
import com.example.bytestore.data.model.cart.toEntity
import com.example.bytestore.data.model.cart.toModel
import com.example.bytestore.data.network.cart.CartService
import com.example.bytestore.data.network.cart.CreateCartItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(private val context: Context) {

    private val dao = DBProvider.getCartDao(context)
    private val service = CartService(context)

    /** Lista observable del carrito (modelo de UI). */
    val cart: LiveData<List<CartItemModel>> =
        dao.observeCart().map { list -> list.map(CartEntity::toModel) }

    /** Subtotal observable en **pesos** (convertido desde centavos). */
    val subtotal: LiveData<Long> =
        dao.observeSubtotalCents().map { cents -> cents / 100 }

    /** Trae el carrito remoto y lo sincroniza localmente. */
    suspend fun pullRemote(): Boolean = withContext(Dispatchers.IO) {
        val remote = service.getCart() ?: return@withContext false

        // Si el servidor responde vacío pero tenemos datos locales,
        // NO sobrescribir. Mantener los datos locales.
        if (remote.data.isEmpty()) {
            val local = dao.getAll()
            if (local.isNotEmpty()) {
                // Intentar crear en el servidor con los datos locales
                val payload = local.map {
                    CreateCartItemDto(it.productId, it.quantity, it.unitPriceCents)
                }
                service.createCartFromPayload(payload)
                // NO borrar la BD local, mantener los datos que ya tenemos
                return@withContext true
            }
            // Si ambos están vacíos, todo OK
            return@withContext true
        }

        // Si el servidor tiene datos, sincronizar PRESERVANDO IMÁGENES LOCALES
        val entities = remote.data.map { serverItem ->
            // Buscar si existe localmente para preservar la imagen
            val localItem = dao.findByProduct(serverItem.productId.toLong())
            serverItem.toEntity().copy(
                imageUrl = localItem?.imageUrl ?: serverItem.image // Preservar imagen local
            )
        }

        val db = DBProvider.get(context)
        db.withTransaction {
            dao.clear()
            dao.insertAll(entities)
        }

        true
    }

    /**
     * Agrega un ítem (o incrementa si ya existe).
     * @param unitPrice precio unitario en PESOS (se convertirá a centavos internamente)
     */
    suspend fun add(productId: Long, name: String, image: String?, unitPrice: Long, qty: Int = 1) {
        withContext(Dispatchers.IO) {
            // unitPrice viene en PESOS, convertir a centavos
            val unitPriceCents = unitPrice * 100

            Log.d(
                "CartRepository",
                "🛒 add() productId=$productId, name=$name, image=$image, qty=$qty"
            )

            // 1) Persistir localmente primero para que la UI lo muestre inmediatamente.
            val prev = dao.findByProduct(productId)

            if (prev == null) {
                val entity = CartEntity(
                    productId = productId,
                    name = name,
                    imageUrl = image, // ✅ GUARDAR IMAGEN
                    unitPriceCents = unitPriceCents,
                    quantity = qty,
                    synced = false
                )
                dao.insertIgnore(entity)
                Log.d("CartRepository", "Producto nuevo insertado con imagen: $image")
            } else {
                // Si ya existe, actualizar cantidad pero PRESERVAR la imagen
                dao.update(
                    prev.copy(
                        quantity = prev.quantity + qty,
                        imageUrl = image ?: prev.imageUrl, // Preservar imagen si viene null
                        synced = false
                    )
                )
                Log.d("CartRepository", "Producto actualizado, imagen preservada: ${prev.imageUrl}")
            }

            // 2) Intentar sincronizar con el servidor en background
            try {
                val serverResult = runCatching {
                    service.addItem(productId, qty, unitPriceCents)
                }.getOrNull()

                if (serverResult != null && serverResult.data.isNotEmpty()) {
                    // VERIFICAR que el servidor devolvió EL PRODUCTO QUE AGREGAMOS
                    val serverHasOurProduct = serverResult.data.any {
                        it.productId.toLong() == productId
                    }

                    if (serverHasOurProduct) {
                        // Solo sincronizar si el servidor tiene nuestro producto
                        val entities = serverResult.data.map { serverItem ->
                            val localItem = dao.findByProduct(serverItem.productId.toLong())
                            serverItem.toEntity().copy(
                                imageUrl = localItem?.imageUrl ?: serverItem.image
                            )
                        }

                        val db = DBProvider.get(context)
                        // No borrar todo: insertar/actualizar solo los items que devuelve el servidor
                        // para evitar perder entradas locales cuando el servidor responde con un
                        // conjunto parcial o distinto.
                        db.withTransaction {
                            dao.insertAll(entities)
                        }
                        Log.d("CartRepository", "Sincronizado con servidor, ${entities.size} items")
                    } else {
                        Log.w(
                            "CartRepository",
                            "Servidor devolvió carrito incorrecto, manteniendo datos locales"
                        )
                    }
                } else {
                    Log.d(
                        "CartRepository",
                        "Servidor sin respuesta válida, manteniendo datos locales"
                    )
                }
            } catch (e: Exception) {
                Log.e("CartRepository", "Error sincronizando: ${e.message}")
                // ignorar errores de red; ya tenemos persistido localmente
            }
        }
    }

    suspend fun increment(productId: Long) {
        val item = dao.findByProduct(productId) ?: return
        val newQty = item.quantity + 1

        // Intentar actualizar en servidor
        val serverResult = service.updateQty(productId, newQty)

        if (serverResult != null && serverResult.data.isNotEmpty()) {
            // Sincronizar preservando imágenes
            val entities = serverResult.data.map { serverItem ->
                val localItem = dao.findByProduct(serverItem.productId.toLong())
                serverItem.toEntity().copy(
                    imageUrl = localItem?.imageUrl ?: serverItem.image
                )
            }
            val db = DBProvider.get(context)
            // Insertar/actualizar items del servidor sin eliminar el resto.
            db.withTransaction {
                dao.insertAll(entities)
            }
        } else {
            // Actualizar solo localmente
            dao.update(item.copy(quantity = newQty, synced = false))
        }
    }

    suspend fun decrement(productId: Long) {
        val item = dao.findByProduct(productId) ?: return
        val newQty = (item.quantity - 1).coerceAtLeast(0)

        if (newQty == 0) {
            // Eliminar
            val serverResult = service.removeItem(productId)
            if (serverResult != null && serverResult.data.isNotEmpty()) {
                val entities = serverResult.data.map { serverItem ->
                    val localItem = dao.findByProduct(serverItem.productId.toLong())
                    serverItem.toEntity().copy(
                        imageUrl = localItem?.imageUrl ?: serverItem.image
                    )
                }
                val db = DBProvider.get(context)
                // Insertar/actualizar items del servidor sin eliminar el resto.
                db.withTransaction {
                    dao.insertAll(entities)
                }
            } else {
                dao.delete(item)
            }
        } else {
            // Decrementar
            val serverResult = service.updateQty(productId, newQty)
            if (serverResult != null && serverResult.data.isNotEmpty()) {
                val entities = serverResult.data.map { serverItem ->
                    val localItem = dao.findByProduct(serverItem.productId.toLong())
                    serverItem.toEntity().copy(
                        imageUrl = localItem?.imageUrl ?: serverItem.image
                    )
                }
                val db = DBProvider.get(context)
                db.withTransaction {
                    dao.clear()
                    dao.insertAll(entities)
                }
            } else {
                dao.update(item.copy(quantity = newQty, synced = false))
            }
        }
    }

    suspend fun remove(productId: Long) {
        val item = dao.findByProduct(productId) ?: return

        val serverResult = service.removeItem(productId)
        if (serverResult != null && serverResult.data.isNotEmpty()) {
            val entities = serverResult.data.map { serverItem ->
                val localItem = dao.findByProduct(serverItem.productId.toLong())
                serverItem.toEntity().copy(
                    imageUrl = localItem?.imageUrl ?: serverItem.image
                )
            }
            val db = DBProvider.get(context)
            db.withTransaction {
                dao.clear()
                dao.insertAll(entities)
            }
        } else {
            dao.delete(item)
        }
    }

    suspend fun clear() {
        service.clear()
        dao.clear()
    }

    /**
     * Obtener snapshot de items desde la BD local (no bloquea la red).
     */
    suspend fun getLocalItems(): List<CartItemModel> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.toModel() }
    }
}