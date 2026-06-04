package com.pzverkov.socialapp.core.sharing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareLinkBuilderTest {

    private val fakeIdProvider = object : InstallationIdProvider {
        override fun get() = "k4ma3x2f"
    }

    private val builder = ShareLinkBuilder(fakeIdProvider)

    @Test
    fun `deeplink has correct format with item id and ref`() {
        val link = builder.buildDeepLink(42)
        assertEquals("socialapp://item/42?ref=k4ma3x2f", link)
    }

    @Test
    fun `share text contains title, price, and deeplink`() {
        val text = builder.buildShareText("Camera", "$150.00", 1)

        assertTrue(text.contains("Camera"))
        assertTrue(text.contains("$150.00"))
        assertTrue(text.contains("socialapp://item/1?ref=k4ma3x2f"))
        assertTrue(text.contains("SocialApp"))
    }

    @Test
    fun `deeplink ref param uses installation id`() {
        val link = builder.buildDeepLink(1)
        assertTrue(link.endsWith("?ref=k4ma3x2f"))
    }

    @Test
    fun `share text has link on separate line`() {
        val text = builder.buildShareText("Bike", "$450.00", 2)
        val lines = text.split("\n")

        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("Bike"))
        assertTrue(lines[1].startsWith("socialapp://"))
    }
}
