@file:OptIn(ExperimentalCoroutinesApi::class)

package org.universe.mojang

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.*

class MojangAPIImplTest {

    private lateinit var mojangApi: MojangAPI

    @BeforeTest
    fun onBefore() {
        val jsonInstance = Json {
            ignoreUnknownKeys = true
        }
        mojangApi = MojangAPIImpl(HttpClient(CIO) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(jsonInstance)
            }
        })
    }

    @Nested
    @DisplayName("Get blocked servers list")
    inner class BlockedServers {

        @Test
        fun `retrieve list`() = runTest {
            val expectedBlockedServers = listOf(
                "c0cbbeafc38c7b7acc5ff58f372e1296e54eebb5",
                "d8143abe910878042250c5d849e9c6b0991b00d5",
                "504af2f6dafe46fe1abf5f4a022afac50bb70dc4",
                "9628751744cdb9f833b1e58b51dbdfca0a0dd722"
            )
            val blockedServers = mojangApi.blockedServers()
            assertTrue { blockedServers.containsAll(expectedBlockedServers) }
        }
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetUUID() {

        @Test
        fun `with an existing player name`() = runTest {
            val name = "lukethehacker23"
            val profileId = mojangApi.getUUID(name)
            assertNotNull(profileId)
            assertEquals(name, profileId.name)
            assertEquals("cdb5aee80f904fdda63ba16d38cd6b3b", profileId.id)
        }

        @Test
        fun `with an unknown player name`() = runTest {
            assertNull(mojangApi.getUUID("a"))
        }

        @Test
        fun `with a player name with invalid length`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(generateUUIDOversize()) }
            }
        }

        @Test
        fun `with a player name with invalid character`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(generateUUIDWithInvalidSymbol()) }
            }
        }
    }

    @Nested
    @DisplayName("Get players uuid from list")
    inner class GetUUIDs() {

        @Test
        fun `without existing player name`() {
            TODO()
        }

        @Test
        fun `too many names`() {
            TODO()
        }

        @Test
        fun `with a player name with invalid length`() {
            TODO()
        }

        @Test
        fun `with a player name with invalid character`() {
            TODO()
        }
    }

    @Nested
    @DisplayName("Get player name")
    inner class GetName() {

        @Test
        fun `with an existing player uuid`() = runTest {
            val uuid = "cdb5aee80f904fdda63ba16d38cd6b3b"
            val profileId = mojangApi.getName(uuid)
            assertNotNull(profileId)
            assertEquals("lukethehacker23", profileId.name)
            assertEquals(uuid, profileId.id)
        }

        @Test
        fun `with an unknown player uuid`() = runTest {
            assertNull(mojangApi.getName(generateRandomUUID()))
        }

        @Test
        fun `with a player uuid with invalid length`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getName(generateUUIDOversize()) }
            }
        }

        @Test
        fun `with a player uuid with invalid character`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getName(generateUUIDWithInvalidSymbol()) }
            }
        }
    }

    @Nested
    @DisplayName("Get player skin")
    inner class GetSkin() {

        @Test
        fun `with an existing player uuid`() = runTest {
            val id = "f1bfcbddc68b49bfaac9fb9d8ce5293d"
            val name = "123lmfao4"
            val skin = mojangApi.getSkin(id)
            assertNotNull(skin)
            assertEquals(id, skin.id)
            assertEquals(name, skin.name)

            val properties = skin.properties
            assertEquals(1, properties.size)

            val property = skin.getTexturesProperty()
            assertEquals("textures", property.name)
            assertNotNull(property.signature)

            val decoded = skin.getSkinDecoded()
            assertEquals(id, decoded.profileId)
            assertEquals(name, decoded.profileName)

            val textures = decoded.textures
            val skinTexture = textures.skin
            assertEquals(
                "http://textures.minecraft.net/texture/e35f3a8df969b56b36f9aa60a736a2f9061de4ccf0fe9657d6c9bc02d77bfd7e",
                skinTexture.url
            )
            assertEquals("slim", skinTexture.metadata.model)
            assertNull(textures.cape)
        }

        @Test
        fun `with an unknown player uuid`() = runTest {
            assertNull(mojangApi.getSkin(generateRandomUUID()))
        }

        @Test
        fun `with a player uuid with invalid length`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getSkin(generateUUIDOversize()) }
            }
        }

        @Test
        fun `with a player uuid with invalid character`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getSkin(generateUUIDWithInvalidSymbol()) }
            }
        }
    }

    @Nested
    @DisplayName("Get player history name")
    inner class GetHistoryName() {

        @Test
        fun `player uuid not exists`() = runTest {
            assertNull(mojangApi.historyName(generateRandomUUID()))
        }

        @Test
        fun `player has never changed name`() = runTest {
            val history = mojangApi.historyName("069a79f444e94726a5befca90e38aaf5")
            assertNotNull(history)
            assertEquals(1, history.size)

            val profileName = history.first()
            assertEquals("Notch", profileName.name)
            assertNull(profileName.changedToAt)
        }

        @Test
        fun `player has changed name several times`() = runTest {
            val history = mojangApi.historyName("b4a93b09-e37c-448f-83ba-0eaf510524b5")
            assertNotNull(history)
            assertEquals(2, history.size)

            val (previous, current) = history
            assertEquals("TIC59000", previous.name)
            assertNull(previous.changedToAt)

            assertEquals("Distractic", current.name)
            assertEquals(1423059429000L, current.changedToAt)
        }

        @Test
        fun `with a player uuid with invalid length`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.historyName(generateUUIDOversize()) }
            }
        }

        @Test
        fun `with a player uuid with invalid character`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getSkin(generateUUIDWithInvalidSymbol()) }
            }
        }
    }

    private fun generateUUIDOversize() = generateRandomUUID() + "a"

    private fun generateUUIDWithInvalidSymbol() = generateRandomUUID().drop(1) + "&"

    private fun generateRandomUUID() = UUID.randomUUID().toString()
}