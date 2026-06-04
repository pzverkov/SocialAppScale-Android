package com.pzverkov.socialapp.core.format

import java.text.NumberFormat
import java.util.Locale

// Locale.US is pinned so the currency (USD) and grouping are deterministic
// regardless of device locale. Widen to (price, currencyCode) once listings
// carry their own currency.
fun formatPrice(price: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(price)
