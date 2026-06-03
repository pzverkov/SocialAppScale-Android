package com.pzverkov.socialapp.core.format

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun `formats whole and fractional amounts as USD`() {
        assertEquals("$150.00", formatPrice(150.0))
        assertEquals("$45.50", formatPrice(45.5))
    }

    @Test
    fun `groups thousands`() {
        assertEquals("$1,234.50", formatPrice(1234.5))
    }
}
