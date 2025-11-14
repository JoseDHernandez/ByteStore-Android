package com.example.bytestore.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bytestore.data.model.cart.CartItemModel
import com.example.bytestore.data.repository.CartRepository
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class CartViewModel(private val repo: CartRepository) : ViewModel() {

    // Estado del carrito con Resource (igual que ProductViewModel)
    private val _state = MutableLiveData<Resource<CartState>>(Resource.Idle)
    val state: LiveData<Resource<CartState>> get() = _state

    // Estado del carrito con los items y totales
    data class CartState(
        val items: List<CartItemModel>,
        val subtotal: Long,      // en centavos
        val shipping: Long,      // en centavos
        val total: Long          // en centavos
    )

    init {
        // Observar cambios en el carrito desde Room
        observeCart()
    }

    // Si el usuario inició un checkout puntual, evitamos que el observer
    // de la BD reemplace inmediatamente ese estado temporal.
    private var tempCheckoutActive: Boolean = false

    private fun observeCart() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                // Usar el LiveData del repositorio
                repo.cart.observeForever { items ->
                    // Si hay un checkout temporal activo, no sobreescribir el estado
                    if (tempCheckoutActive) return@observeForever

                    // Calcular subtotal (evitar truncamiento por conversión float->long)
                    val subtotal = items.sumOf { ((it.unitPrice * 100).roundToLong() * it.quantity).toLong() }

                    // CORREGIDO: Shipping es $16.000 = 1,600,000 centavos
                    val shipping = if (items.isEmpty()) 0L else 1600000L
                    val total = subtotal + shipping

                    val cartState = CartState(
                        items = items,
                        subtotal = subtotal,
                        shipping = shipping,
                        total = total
                    )
                    _state.postValue(Resource.Success(cartState))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(Resource.Error("Error al cargar el carrito: ${e.message}"))
            }
        }
    }

    fun pullRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(Resource.Loading)
            try {
                val success = repo.pullRemote()
                if (!success) {
                    _state.postValue(Resource.Error("Error al sincronizar con el servidor"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    fun add(productId: Long, name: String, image: String?, unitPrice: Long, qty: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.add(productId, name, image, unitPrice, qty)
                // Forzar la recarga desde la BD local para asegurar que la UI
                // vea inmediatamente el cambio (no confiar sólo en observadores).
                loadLocalSnapshot()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(Resource.Error("Error al agregar: ${e.message}"))
            }
        }
    }

    fun inc(productId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.increment(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dec(productId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.decrement(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun remove(productId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.remove(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clear() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Preparar un estado temporal de carrito con un solo item para permitir
     * comprar directamente sin persistir en la base local.
     * unitPricePesos: precio por unidad en PESOS (no en centavos).
     */
    fun startCheckoutWithItem(productId: Long, name: String, image: String?, unitPricePesos: Long, qty: Int = 1) {
        // Construir un CartItemModel temporal
        val item = CartItemModel(
            id = 0,
            productId = productId.toInt(),
            name = name,
            image = image,
            unitPrice = unitPricePesos.toFloat(),
            quantity = qty,
            userId = null,
            synced = false,
            updatedAt = System.currentTimeMillis()
        )

        // subtotal/total en CENTAVOS
        val subtotal = (unitPricePesos * 100L) * qty
        // CORREGIDO: Shipping es $16.000 = 1,600,000 centavos
        val shipping = if (qty > 0) 1600000L else 0L
        val total = subtotal + shipping

        val cartState = CartState(
            items = listOf(item),
            subtotal = subtotal,
            shipping = shipping,
            total = total
        )

        tempCheckoutActive = true
        _state.postValue(Resource.Success(cartState))
    }

    fun finishTemporaryCheckout() {
        tempCheckoutActive = false
        // después de liberar, forzar recálculo desde la BD
        viewModelScope.launch(Dispatchers.Main) {
            repo.cart.value?.let { items ->
                val subtotal = items.sumOf { ((it.unitPrice * 100).roundToLong() * it.quantity).toLong() }
                // CORREGIDO: Shipping es $16.000 = 1,600,000 centavos
                val shipping = if (items.isEmpty()) 0L else 1600000L
                val total = subtotal + shipping
                _state.postValue(Resource.Success(CartState(items, subtotal, shipping, total)))
            }
        }
    }

    /**
     * Cargar un snapshot de la BD local y publicar el estado si hay items.
     * Útil cuando la sincronización remota devuelve vacío pero queremos mostrar
     * lo que hay guardado localmente.
     */
    fun loadLocalSnapshot() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val local = repo.getLocalItems()
                val subtotal = local.sumOf { ((it.unitPrice * 100).roundToLong() * it.quantity).toLong() }
                // CORREGIDO: Shipping es $16.000 = 1,600,000 centavos
                val shipping = if (local.isEmpty()) 0L else 1600000L
                val total = subtotal + shipping
                val cartState = CartState(items = local, subtotal = subtotal, shipping = shipping, total = total)
                _state.postValue(Resource.Success(cartState))
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(Resource.Error("Error al cargar: ${e.message}"))
            }
        }
    }
}