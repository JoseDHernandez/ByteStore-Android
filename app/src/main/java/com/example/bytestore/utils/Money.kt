package com.example.bytestore.utils



import java.text.NumberFormat
import java.util.Locale

fun centsToMoney(value: Long, locale: Locale = Locale("es","CO"), showCents: Boolean = true): String {
  val nf = NumberFormat.getCurrencyInstance(locale)
  if (!showCents) {
    // Mostrar sin decimales (por ejemplo para COP)
    nf.maximumFractionDigits = 0
    nf.minimumFractionDigits = 0
  } else {
    nf.maximumFractionDigits = 2
    nf.minimumFractionDigits = 2
  }
  return nf.format(value / 100.0)
}
