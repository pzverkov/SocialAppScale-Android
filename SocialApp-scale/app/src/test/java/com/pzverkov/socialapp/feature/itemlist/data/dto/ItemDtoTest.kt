package com.pzverkov.socialapp.feature.itemlist.data.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemDtoTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = ItemDto(
            id = 42,
            title = "Camera",
            description = "Vintage",
            price = 99.99,
            imageUrl = "https://img.jpg",
            location = "London",
        )

        val domain = dto.toDomain()

        assertEquals(42, domain.id)
        assertEquals("Camera", domain.title)
        assertEquals("Vintage", domain.description)
        assertEquals(99.99, domain.price, 0.001)
        assertEquals("https://img.jpg", domain.imageUrl)
        assertEquals("London", domain.location)
    }

    @Test
    fun `deserializes from JSON matching mock server schema`() {
        val json = """
            {
                "id": 1,
                "title": "Vintage Camera",
                "description": "A beautiful vintage camera",
                "price": 150.00,
                "imageUrl": "https://picsum.photos/seed/camera1/400/400",
                "location": "New York"
            }
        """.trimIndent()

        val dto = Json.decodeFromString<ItemDto>(json)

        assertEquals(1, dto.id)
        assertEquals("Vintage Camera", dto.title)
        assertEquals(150.0, dto.price, 0.001)
        assertEquals("New York", dto.location)
    }

    @Test
    fun `deserializes list matching mock server response`() {
        val json = """
            [
                {"id":1,"title":"A","description":"d","price":10.0,"imageUrl":"","location":"X"},
                {"id":2,"title":"B","description":"d","price":20.0,"imageUrl":"","location":"Y"}
            ]
        """.trimIndent()

        val list = Json.decodeFromString<List<ItemDto>>(json)

        assertEquals(2, list.size)
        assertEquals("A", list[0].title)
        assertEquals("B", list[1].title)
    }
}
