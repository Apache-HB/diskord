package com.serebit.strife.internal.network

import com.serebit.strife.internal.packets.*
import io.ktor.http.HttpMethod
import kotlinx.serialization.KSerializer

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
        HttpMethod.Get, "channels/$channelID", GenericChannelPacket.serializer(),
        channelID
    )

    class GetTextChannel(channelID: Long) : Endpoint<GenericTextChannelPacket>(
        HttpMethod.Get, "channels/$channelID", GenericTextChannelPacket.serializer(),
        channelID
    )

    class GetDmChannel(channelID: Long) : Endpoint<DmChannelPacket>(
        HttpMethod.Get, "channels/$channelID", DmChannelPacket.serializer(),
        channelID
    )

    class GetGuildTextChannel(channelID: Long) : Endpoint<GuildTextChannelPacket>(
        HttpMethod.Get, "channels/$channelID", GuildTextChannelPacket.serializer(),
        channelID
    )

    class GetGuildVoiceChannel(channelID: Long) : Endpoint<GuildVoiceChannelPacket>(
        HttpMethod.Get, "channels/$channelID", GuildVoiceChannelPacket.serializer(),
        channelID
    )

    class GetGuildChannelCategory(channelID: Long) : Endpoint<GuildChannelCategoryPacket>(
        HttpMethod.Get, "channels/$channelID", GuildChannelCategoryPacket.serializer(),
        channelID
    )

    object CreateDmChannel : Endpoint<DmChannelPacket>(
        HttpMethod.Post, "users/@me/channels", DmChannelPacket.serializer()
    )

    class CreateGuildChannel(guildID: Long) : Endpoint<GenericChannelPacket>(
        HttpMethod.Post, "guilds/$guildID/channels", GenericChannelPacket.serializer(),
        guildID
    )

    // Gateway Endpoints

    object GetGateway : Endpoint<Unit>(HttpMethod.Get, "gateway")

    object GetGatewayBot : Endpoint<Unit>(HttpMethod.Get, "gateway/bot")

    // Guild Endpoints

    class GetGuild(guildID: Long) : Endpoint<GuildCreatePacket>(
        HttpMethod.Get, "/guilds/$guildID", GuildCreatePacket.serializer(),
        guildID
    )

    object CreateGuild : Endpoint<GuildCreatePacket>(HttpMethod.Post, "/guilds", GuildCreatePacket.serializer())

    class CreateRole(guildID: Long) : Endpoint<RolePacket>(
        HttpMethod.Post, "guilds/$guildID/roles", RolePacket.serializer(),
        guildID
    )

    class KickGuildMember(guildID: Long, userID: Long) : Endpoint<Unit>(
        HttpMethod.Delete, "guilds/$guildID/members/$userID",
        guildID
    )

    class BanGuildMember(guildID: Long, userID: Long) : Endpoint<Unit>(
        HttpMethod.Put, "guilds/$guildID/bans/$userID",
        guildID
    )

    // Message Endpoints

    class GetMessage(channelID: Long, messageID: Long) : Endpoint<MessageCreatePacket>(
        HttpMethod.Get, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
        channelID
    )

    class CreateMessage(channelID: Long) : Endpoint<MessageCreatePacket>(
        HttpMethod.Post, "channels/$channelID/messages", MessageCreatePacket.serializer(),
        channelID
    )

    class EditMessage(channelID: Long, messageID: Long) : Endpoint<MessageCreatePacket>(
        HttpMethod.Patch, "channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
        channelID
    )

    class DeleteMessage(channelID: Long, messageID: Long) : Endpoint<Unit>(
        HttpMethod.Delete, "channels/$channelID/messages/$messageID",
        channelID
    )

    // User Endpoints

    object GetSelfUser : Endpoint<UserPacket>(HttpMethod.Get, "users/@me", UserPacket.serializer())

    class GetUser(userID: Long) : Endpoint<UserPacket>(HttpMethod.Get, "users/$userID", UserPacket.serializer())

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion/"
    }
}
