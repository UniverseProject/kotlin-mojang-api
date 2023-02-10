package io.github.universeproject.kotlinmojangapi.serializer

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class StringUUIDSerializerTest {

    @Nested
    inner class Serialize {

        @Test
        fun `should serialize a valid uuid with dashes`() {
            val uuid = UUID.randomUUID().toString()
            val serializer = StringUUIDSerializer
            assertEquals(stringWrappedInQuotes(uuid), Json.encodeToString(serializer, uuid))
        }

        @Test
        fun `should serialize a valid uuid without dashes`() {
            val expected = UUID.randomUUID().toString()
            val uuidWithoutDashes = expected.replace("-", "")
            val serializer = StringUUIDSerializer
            assertEquals(stringWrappedInQuotes(expected), Json.encodeToString(serializer, uuidWithoutDashes))
        }

        @Test
        fun `should serialize a valid uuid with uppercase letters and with dashes`() {
            val expected = UUID.randomUUID().toString().uppercase(Locale.getDefault())
            val serializer = StringUUIDSerializer
            assertEquals(stringWrappedInQuotes(expected), Json.encodeToString(serializer, expected))
        }

        @Test
        fun `should serialize a valid uuid with uppercase letters and without dashes`() {
            val expected = UUID.randomUUID().toString().uppercase(Locale.getDefault())
            val uuidWithoutDashes = expected.replace("-", "")
            val serializer = StringUUIDSerializer
            assertEquals(stringWrappedInQuotes(expected), Json.encodeToString(serializer, uuidWithoutDashes))
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
                assertThrows<IllegalArgumentException> { Json.encodeToString(StringUUIDSerializer, it) }
            }
        }

        private fun stringWrappedInQuotes(string: String) = "\"$string\""

    }

    @Nested
    inner class Deserialize {

        @Test
        fun `should deserialize a valid uuid with dashes`() {
            val uuid = UUID.randomUUID().toString()
            val serializer = StringUUIDSerializer
            assertEquals(uuid, Json.decodeFromString(serializer, wrappedInQuotes(uuid)))
        }

        @Test
        fun `should deserialize a valid uuid without dashes`() {
            val expected = UUID.randomUUID().toString()
            val uuidWithoutDashes = expected.replace("-", "")
            val serializer = StringUUIDSerializer
            assertEquals(expected, Json.decodeFromString(serializer, wrappedInQuotes(uuidWithoutDashes)))
        }

        @Test
        fun `should deserialize a valid uuid with uppercase letters and with dashes`() {
            val expected = UUID.randomUUID().toString().uppercase(Locale.getDefault())
            val serializer = StringUUIDSerializer
            assertEquals(expected, Json.decodeFromString(serializer, wrappedInQuotes(expected)))
        }

        @Test
        fun `should deserialize a valid uuid with uppercase letters and without dashes`() {
            val expected = UUID.randomUUID().toString().uppercase(Locale.getDefault())
            val uuidWithoutDashes = expected.replace("-", "")
            val serializer = StringUUIDSerializer
            assertEquals(expected, Json.decodeFromString(serializer, wrappedInQuotes(uuidWithoutDashes)))
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
                assertThrows<IllegalArgumentException> { Json.decodeFromString(StringUUIDSerializer, wrappedInQuotes(it)) }
            }
        }

        private fun wrappedInQuotes(string: String) = "\"$string\""

    }
}