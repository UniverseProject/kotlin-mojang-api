package io.github.universeproject

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Allows interact with Mojang API.
 */
public interface MojangAPI {

    // Sale statistics endpoint https://mojang-api-docs.netlify.app/no-auth/sale-stats.html
    // is no longer valid with the defined parameters

    /**
     * Retrieve a hashed information for all blocked server by Mojang.
     * https://mojang-api-docs.netlify.app/no-auth/blocked-servers.html
     * @return List of hash.
     */
    public suspend fun getBlockedServers(): List<String>

    /**
     * Checks if a username is available or taken.
     * https://mojang-api-docs.netlify.app/no-auth/username-availability-accurate.html
     * Don't trust the doc - the response has changed.
     * @param name Player's name.
     * @return `true` if the name is available, `false` otherwise.
     */
    public suspend fun isUsernameAvailable(name: String): Boolean

    /**
     * Allows users to supply a username to be checked and get its UUID if the username resolves to a valid Minecraft profile.
     * https://mojang-api-docs.netlify.app/no-auth/username-to-uuid-get.html
     * @param name Player's name.
     * @return Instance of [ProfileId] linked to the player if found, `null` otherwise.
     */
    public suspend fun getUUID(name: String): ProfileId?

    /**
     * Allows users to send up to 10 usernames in an array and return all valid UUIDs
     * https://mojang-api-docs.netlify.app/no-auth/username-to-uuid-post.html
     * @param names Players' names
     * @return Instances of [ProfileId] linked to each existing player.
     */
    public suspend fun getUUID(names: Collection<String>): List<ProfileId>

    /**
     * Allows users to supply a UUID to be checked and get its username if the UUID resolves to a valid Minecraft profile.
     * https://mojang-api-docs.netlify.app/no-auth/uuid-to-username.html
     * @param uuid Player's uuid.
     * @return Instance of [ProfileId] linked to the player if found, `null` otherwise.
     */
    public suspend fun getName(uuid: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * https://mojang-api-docs.netlify.app/no-auth/uuid-to-profile.html
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    public suspend fun getSkin(uuid: String): ProfileSkin?

    /**
     * Allows users to find the username history of a Minecraft profile
     * https://mojang-api-docs.netlify.app/no-auth/name-history.html
     * @param uuid Player's UUID.
     * @return List of [ProfileName].
     * The first name is the older, so the [ProfileName.changedToAt] is null, the following names
     * are the most recent and contain all a value for [ProfileName.changedToAt].
     * The function returns `null` if the uuid is not linked to a player.
     */
    public suspend fun getHistoryName(uuid: String): List<ProfileName>?
}

/**
 * Implementation to interact with Mojang API using a custom coroutine client.
 * @property client Coroutine http client used to interact with api.
 */
public class MojangAPIImpl(private val client: HttpClient) : MojangAPI {

    override suspend fun isUsernameAvailable(name: String): Boolean {
        val response = client.get("https://account.mojang.com/available/minecraft/${name}")
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun getBlockedServers(): List<String> {
        return client.get("https://sessionserver.mojang.com/blockedservers") {
          accept(ContentType.Text.Plain)
        }.bodyAsText().lines()
    }

    override suspend fun getUUID(name: String): ProfileId? {
        val response = client.get("https://api.mojang.com/user/profile/agent/minecraft/name/$name")
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun getUUID(names: Collection<String>): List<ProfileId> {
        return client.post("https://api.mojang.com/profiles/minecraft") {
            contentType(ContentType.Application.Json)
            setBody(names)
        }.body()
    }

    override suspend fun getName(uuid: String): ProfileId? {
        val response = client.get("https://api.mojang.com/user/profile/$uuid")
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        val response = client.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid?unsigned=false")
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun getHistoryName(uuid: String): List<ProfileName>? {
        val response = client.get("https://api.mojang.com/user/profile/${uuid}/names")
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

}