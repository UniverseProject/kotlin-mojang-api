package io.github.universeproject.kotlinmojangapi.extension

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtTest {

    @Nested
    inner class FormatUUID {

        @Test
        fun `should return the same string if it is already formatted`() {
            repeat(100) {
                val uuid = UUID.randomUUID().toString()
                assertEquals(uuid, uuid.formatUUID())
            }
        }

        @Test
        fun `should throw an exception if the string is formatted but with invalid character`() {
            sequenceOf(
                "123y5678-1234-1234-1234-123456789012",
                "12345678-12g4-1234-1234-123456789012",
                "12345678-1234-12q4-1234-123456789012",
                "12345678-1234-1234-12x4-123456789012",
                "12345678-1234-1234-1234-12345678901g"
            ).forEach {
                val ex = assertThrows<IllegalArgumentException> { it.formatUUID() }
                assertEquals("Invalid UUID format for string: [$it]", ex.message)
            }
        }

        @Test
        fun `should return the formatted string with a uuid format`() {
            val unformatted = "12345678123412341234123456789012"
            val formatted = "12345678-1234-1234-1234-123456789012"
            assertEquals(formatted, unformatted.formatUUID())
        }

        @Test
        fun `should return the formatted string with a uuid format with uppercase letters`() {
            val unformatted = "aCfE1234-12X4-1wW4-12F4-123456T89012"
            val formatted = "aCfE1234-12X4-1wW4-12F4-123456T89012"
            assertEquals(formatted, unformatted.formatUUID())
        }
    }
}