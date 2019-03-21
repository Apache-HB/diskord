package com.serebit.strife.internal.network

import com.serebit.strife.internal.packets.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import kotlinx.serialization.KSerializer

/**
 * An [Endpoint] is used to create [HTTP Requests][Requester] to the Discord API.
 *
 * @param T the expected packet type of the endpoint [Response]
 * @property method The [HttpMethod] type for the endpoint (GET, POST, etc.)
 * @property path The uri path of the request
 * @property serializer the [KSerializer] used to deserialize the incoming packet
 * @property majorParameters url path parameters which are used to build the [path]
 */
internal sealed class Endpoint<T>(
    val method: HttpMethod,
    private val path: String,
    val serializer: KSerializer<T>?,
    val majorParameters: List<Long>
) {
    val uri get() = "$baseUri$path"

    constructor(method: HttpMethod, path: String, serializer: KSerializer<T>, vararg majorParameters: Long) :
            this(method, path, serializer, majorParameters.toList())

    constructor(method: HttpMethod, path: String, vararg majorParameters: Long) :
            this(method, path, null, majorParameters.toList())

    // Channel Endpoints

    class GetChannel(channelID: Long) : Endpoint<GenericChannelPacket>(
        Get, "channels/$channelID", GenericChannelPacket.serializer(), channelID
    )

    class GetTextChannel(channelID: Long) : Endpoint<GenericTextChannelPacket>(
        Get, "channels/$channelID", GenericTextChannelPacket.serializer(), channelID
    )

    class GetDmChannel(channelID: Long) : Endpoint<DmChannelPacket>(
        Get, "channels/$channelID", DmChannelPacket.serializer(), channelID
    )

    class GetGuildTextChannel(channelID: Long) : Endpoint<GuildTextChannelPacket>(
        Get, "channels/$channelID", GuildTextChannelPacket.serializer(), channelID
    )

    class GetGuildVoiceChannel(channelID: Long) : Endpoint<GuildVoiceChannelPacket>(
        Get, "channels/$channelID", GuildVoiceChannelPacket.serializer(), channelID
    )

    class GetGuildChannelCategory(channelID: Long) : Endpoint<GuildChannelCategoryPacket>(
        Get, "channels/$channelID", GuildChannelCategoryPacket.serializer(), channelID
    )

    object CreateDmChannel : Endpoint<DmChannelPacket>(
        Post, "users/@me/channels", DmChannelPacket.serializer()
    )

    class CreateGuildChannel(guildID: Long) : Endpoint<GenericChannelPacket>(
        Post, "guilds/$guildID/channels", GenericChannelPacket.serializer(), guildID
    )

    // Gateway Endpoints

    object GetGateway : Endpoint<Unit>(Get, "gateway")

    object GetGatewayBot : Endpoint<Unit>(Get, "gateway/bot")

    // Guild Endpoints

    class GetGuild(guildID: Long) : Endpoint<GuildCreatePacket>(
        Get, "/guilds/$guildID", GuildCreatePacket.serializer(), guildID
    )

    object CreateGuild : Endpoint<GuildCreatePacket>(Post, "/guilds", GuildCreatePacket.serializer())

    class CreateRole(guildID: Long) : Endpoint<RolePacket>(
        Post, "guilds/$guildID/roles", RolePacket.serializer(), guildID
    )

    class KickGuildMember(guildID: Long, userID: Long) : Endpoint<Unit>(
        Delete, "guilds/$guildID/members/$userID", guildID
    )

    class BanGuildMember(guildID: Long, userID: Long) : Endpoint<Unit>(
        Put, "guilds/$guildID/bans/$userID", guildID
    )

    // Message Endpoints

    class GetMessage(channelID: Long, messageID: Long) : Endpoint<MessageCreatePacket>(
        Get, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(), channelID
    )

    class CreateMessage(channelID: Long) : Endpoint<MessageCreatePacket>(
        Post, "channels/$channelID/messages", MessageCreatePacket.serializer(), channelID
    )

    class EditMessage(channelID: Long, messageID: Long) : Endpoint<MessageCreatePacket>(
        Patch, "channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(), channelID
    )

    class DeleteMessage(channelID: Long, messageID: Long) : Endpoint<Unit>(
        Delete, "channels/$channelID/messages/$messageID", channelID
    )

    // User Endpoints

    object GetSelfUser : Endpoint<UserPacket>(Get, "users/@me", UserPacket.serializer())

    class GetUser(userID: Long) : Endpoint<UserPacket>(Get, "users/$userID", UserPacket.serializer())

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion/"
    }
}
