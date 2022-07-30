@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.universeproject

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
    @DisplayName("Check if username is available")
    inner class UsernameAvailable {

        @Test
        fun `username not available`() = runTest {
            assertFalse { mojangApi.usernameAvailable("Notch") }
        }

        @Test
        fun `username is available`() = runTest {
            assertTrue { mojangApi.usernameAvailable(generateRandomName()) }
        }

        @Test
        fun `with a player name with invalid length`() = runTest {
            assertTrue { mojangApi.usernameAvailable("a") }
        }

        @Test
        fun `with a player name with invalid character`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.usernameAvailable(generateRandomNameWithInvalidSymbol()) }
            }
        }
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetUUID() {

        @Test
        fun `with an existing player name`() = runTest {
            val name = "lukethehacker23"
            assertEquals(ProfileId(name, "cdb5aee80f904fdda63ba16d38cd6b3b"), mojangApi.getUUID(name))
        }

        @Test
        fun `with an unknown player name`() = runTest {
            assertNull(mojangApi.getUUID(generateRandomName()))
        }

        @Test
        fun `with a player name with invalid length`() = runTest {
            assertNull(mojangApi.getUUID("a"))
        }

        @Test
        fun `with a player name with invalid character`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(generateRandomNameWithInvalidSymbol()) }
            }
        }
    }

    @Nested
    @DisplayName("Get players uuid from list")
    inner class GetUUIDs() {

        private val limitNumberOfName = 10

        @Test
        fun `without player name`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(emptyList()) }
            }
        }

        @Test
        fun `with non existing player name`() = runTest {
            val profiles = mojangApi.getUUID(listOf(generateRandomName()))
            assertTrue { profiles.isEmpty() }
        }

        @Test
        fun `with existing player name`() = runTest {
            assertContentEquals(
                listOf(
                    ProfileId("jeb_", "853c80ef3c3749fdaa49938b674adae6"),
                    ProfileId("Notch", "069a79f444e94726a5befca90e38aaf5")
                ),
                mojangApi.getUUID(listOf("Notch", "jeb_"))
            )
        }

        @Test
        fun `with existing and non existing player name`() = runTest {
            assertContentEquals(
                listOf(
                    ProfileId("jeb_", "853c80ef3c3749fdaa49938b674adae6"),
                ),
                mojangApi.getUUID(listOf(generateRandomName(), "jeb_", generateRandomName()))
            )
        }

        @Test
        fun `too many names`() {
            val names = List(limitNumberOfName + 1) { generateRandomName() }
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(names) }
            }
        }

        @Test
        fun `with a player name with invalid length`() = runTest {
            assertEquals(emptyList(), mojangApi.getUUID(listOf("a")))
        }

        @Test
        fun `with a player name with invalid character`() {
            val names = listOf(generateRandomNameWithInvalidSymbol())
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(names) }
            }
        }
    }

    @Nested
    @DisplayName("Get player name")
    inner class GetName() {

        @Test
        fun `with an existing player uuid`() = runTest {
            val uuid = "cdb5aee80f904fdda63ba16d38cd6b3b"
            assertEquals(ProfileId("lukethehacker23", uuid), mojangApi.getName(uuid))
        }

        @Test
        fun `with an unknown player uuid`() = runTest {
            assertNull(mojangApi.getName(generateRandomUUID()))
        }

        @Test
        fun `with a player uuid with invalid length`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getName("a") }
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
                runBlocking { mojangApi.getSkin("a") }
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
            assertContentEquals(
                listOf(
                    ProfileName("Notch", 0)
                ),
                mojangApi.historyName("069a79f444e94726a5befca90e38aaf5")
            )
        }

        @Test
        fun `player has changed name several times`() = runTest {
            assertContentEquals(
                listOf(
                    ProfileName("TIC59000", 0),
                    ProfileName("Distractic", 1423059429000)
                ),
                mojangApi.historyName("b4a93b09-e37c-448f-83ba-0eaf510524b5")
            )
        }

        @Test
        fun `with a player uuid with invalid length`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.historyName("a") }
            }
        }

        @Test
        fun `with a player uuid with invalid character`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.historyName(generateUUIDWithInvalidSymbol()) }
            }
        }
    }

    private fun generateRandomNameWithInvalidSymbol() = generateRandomName().drop(1) + "&"

    private fun generateRandomName(): String {
        val validCharRanges: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return generateSequence { validCharRanges.random() }.take(16).joinToString("")
    }

    private fun generateUUIDWithInvalidSymbol() = generateRandomUUID().drop(1) + "&"

    private fun generateRandomUUID() = UUID.randomUUID().toString()
}