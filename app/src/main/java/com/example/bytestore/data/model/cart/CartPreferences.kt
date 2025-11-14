package com.example.bytestore.data.model.cart

enum class DeliveryMethod { HOME, PICKUP }
enum class PaymentMethod { CARD, PSE, CASH }

data class CartPreferences(
  val deliveryMethod: DeliveryMethod = DeliveryMethod.HOME,
  val paymentMethod: PaymentMethod = PaymentMethod.CASH,
  val address: String? = null
)
