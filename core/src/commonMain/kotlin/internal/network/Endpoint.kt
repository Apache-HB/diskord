package com.serebit.strife.internal.network

import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.serebit.strife.internal.packets.GenericTextChannelPacket
import com.serebit.strife.internal.packets.GroupDmChannelPacket
import com.serebit.strife.internal.packets.GuildChannelCategoryPacket
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import com.serebit.strife.internal.packets.GuildVoiceChannelPacket
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.RolePacket
import com.serebit.strife.internal.packets.UserPacket
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import kotlinx.serialization.KSerializer

/**
 * An [Endpoint] is a type-safe object used to create [HTTP Requests][Requester] to the Discord API.
 *
 * @param T the expected packet type of the endpoint [Response]
 * @property method The [HttpMethod] type for the endpoint (GET, POST, etc.)
 * @property path The uri path of the request
 * @property serializer the [KSerializer] used to deserialize the incoming packet
 * @property majorParameters url path parameters which are used to build the [path]
 */
internal sealed class Endpoint<T>( //TODO Enforce Packet type on <T>!
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

    class GetChannel(channelId: Long) : Endpoint<GenericChannelPacket>(
        Get, "channels/$channelId", GenericChannelPacket.serializer(), channelId
    )

    class GetTextChannel(channelId: Long) : Endpoint<GenericTextChannelPacket>(
        Get, "channels/$channelId", GenericTextChannelPacket.serializer(), channelId
    )

    class GetDmChannel(channelId: Long) : Endpoint<DmChannelPacket>(
        Get, "channels/$channelId", DmChannelPacket.serializer(), channelId
    )

    class GetGroupDmChannel(channelId: Long) : Endpoint<GroupDmChannelPacket>(
        Get, "channels/$channelId", GroupDmChannelPacket.serializer(), channelId
    )

    class GetGuildTextChannel(channelId: Long) : Endpoint<GuildTextChannelPacket>(
        Get, "channels/$channelId", GuildTextChannelPacket.serializer(), channelId
    )

    class GetGuildVoiceChannel(channelId: Long) : Endpoint<GuildVoiceChannelPacket>(
        Get, "channels/$channelId", GuildVoiceChannelPacket.serializer(), channelId
    )

    class GetGuildChannelCategory(channelId: Long) : Endpoint<GuildChannelCategoryPacket>(
        Get, "channels/$channelId", GuildChannelCategoryPacket.serializer(), channelId
    )

    object CreateDmChannel : Endpoint<DmChannelPacket>(
        Post, "users/@me/channels", DmChannelPacket.serializer()
    )

    class CreateGuildChannel(guildId: Long) : Endpoint<GenericChannelPacket>(
        Post, "guilds/$guildId/channels", GenericChannelPacket.serializer(), guildId
    )

    // Gateway Endpoints

    object GetGateway : Endpoint<Unit>(Get, "gateway")

    object GetGatewayBot : Endpoint<Unit>(Get, "gateway/bot")

    // Guild Endpoints

    class GetGuild(guildId: Long) : Endpoint<GuildCreatePacket>(
        Get, "/guilds/$guildId", GuildCreatePacket.serializer(), guildId
    )

    object CreateGuild : Endpoint<GuildCreatePacket>(Post, "/guilds", GuildCreatePacket.serializer())

    class CreateRole(guildId: Long) : Endpoint<RolePacket>(
        Post, "guilds/$guildId/roles", RolePacket.serializer(), guildId
    )

    class KickGuildMember(guildId: Long, userId: Long) : Endpoint<Unit>(
        Delete, "guilds/$guildId/members/$userId", guildId
    )

    class BanGuildMember(guildId: Long, userId: Long) : Endpoint<Unit>(
        Put, "guilds/$guildId/bans/$userId", guildId
    )

    // Message Endpoints

    class GetMessage(channelId: Long, messageId: Long) : Endpoint<MessageCreatePacket>(
        Get, "/channels/$channelId/messages/$messageId", MessageCreatePacket.serializer(), channelId
    )

    class CreateMessage(channelId: Long) : Endpoint<MessageCreatePacket>(
        Post, "channels/$channelId/messages", MessageCreatePacket.serializer(), channelId
    )

    class EditMessage(channelId: Long, messageId: Long) : Endpoint<MessageCreatePacket>(
        Patch, "channels/$channelId/messages/$messageId", MessageCreatePacket.serializer(), channelId
    )

    class DeleteMessage(channelId: Long, messageId: Long) : Endpoint<Unit>(
        Delete, "channels/$channelId/messages/$messageId", channelId
    )

    // User Endpoints

    object GetSelfUser : Endpoint<UserPacket>(Get, "users/@me", UserPacket.serializer())

    class GetUser(userId: Long) : Endpoint<UserPacket>(Get, "users/$userId", UserPacket.serializer())

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion/"
    }
}
