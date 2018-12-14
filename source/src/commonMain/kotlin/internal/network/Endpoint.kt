package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GenericChannelPacket
import com.serebit.diskord.internal.packets.GenericTextChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelCategoryPacket
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.RolePacket
import com.serebit.diskord.internal.packets.UserPacket
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

    class GetChannel(channelId: Long) : Endpoint<GenericChannelPacket>(
        HttpMethod.Get, "channels/$channelId", GenericChannelPacket.serializer(),
        channelId
    )

    class GetTextChannel(channelId: Long) : Endpoint<GenericTextChannelPacket>(
        HttpMethod.Get, "channels/$channelId", GenericTextChannelPacket.serializer(),
        channelId
    )

    class GetDmChannel(channelId: Long) : Endpoint<DmChannelPacket>(
        HttpMethod.Get, "channels/$channelId", DmChannelPacket.serializer(),
        channelId
    )

    class GetGroupDmChannel(channelId: Long) : Endpoint<GroupDmChannelPacket>(
        HttpMethod.Get, "channels/$channelId", GroupDmChannelPacket.serializer(),
        channelId
    )

    class GetGuildTextChannel(channelId: Long) : Endpoint<GuildTextChannelPacket>(
        HttpMethod.Get, "channels/$channelId", GuildTextChannelPacket.serializer(),
        channelId
    )

    class GetGuildVoiceChannel(channelId: Long) : Endpoint<GuildVoiceChannelPacket>(
        HttpMethod.Get, "channels/$channelId", GuildVoiceChannelPacket.serializer(),
        channelId
    )

    class GetGuildChannelCategory(channelId: Long) : Endpoint<GuildChannelCategoryPacket>(
        HttpMethod.Get, "channels/$channelId", GuildChannelCategoryPacket.serializer(),
        channelId
    )

    object CreateDmChannel : Endpoint<DmChannelPacket>(
        HttpMethod.Post, "users/@me/channels", DmChannelPacket.serializer()
    )

    class CreateGuildChannel(guildId: Long) : Endpoint<GenericChannelPacket>(
        HttpMethod.Post, "guilds/$guildId/channels", GenericChannelPacket.serializer(),
        guildId
    )

    // Gateway Endpoints

    object GetGateway : Endpoint<Unit>(HttpMethod.Get, "gateway")

    object GetGatewayBot : Endpoint<Unit>(HttpMethod.Get, "gateway/bot")

    // Guild Endpoints

    class GetGuild(guildId: Long) : Endpoint<GuildCreatePacket>(
        HttpMethod.Get, "/guilds/$guildId", GuildCreatePacket.serializer(),
        guildId
    )

    object CreateGuild : Endpoint<GuildCreatePacket>(HttpMethod.Post, "/guilds", GuildCreatePacket.serializer())

    class CreateRole(guildId: Long) : Endpoint<RolePacket>(
        HttpMethod.Post, "guilds/$guildId/roles", RolePacket.serializer(),
        guildId
    )

    class KickGuildMember(guildId: Long, userId: Long) : Endpoint<Unit>(
        HttpMethod.Delete, "guilds/$guildId/members/$userId",
        guildId
    )

    class BanGuildMember(guildId: Long, userId: Long) : Endpoint<Unit>(
        HttpMethod.Put, "guilds/$guildId/bans/$userId",
        guildId
    )

    // Message Endpoints

    class GetMessage(channelId: Long, messageId: Long) : Endpoint<MessageCreatePacket>(
        HttpMethod.Get, "/channels/$channelId/messages/$messageId", MessageCreatePacket.serializer(),
        channelId
    )

    class CreateMessage(channelId: Long) : Endpoint<MessageCreatePacket>(
        HttpMethod.Post, "channels/$channelId/messages", MessageCreatePacket.serializer(),
        channelId
    )

    class EditMessage(channelId: Long, messageId: Long) : Endpoint<MessageCreatePacket>(
        HttpMethod.Patch, "channels/$channelId/messages/$messageId", MessageCreatePacket.serializer(),
        channelId
    )

    class DeleteMessage(channelId: Long, messageId: Long) : Endpoint<Unit>(
        HttpMethod.Delete, "channels/$channelId/messages/$messageId",
        channelId
    )

    // User Endpoints

    object GetSelfUser : Endpoint<UserPacket>(HttpMethod.Get, "users/@me", UserPacket.serializer())

    class GetUser(userId: Long) : Endpoint<UserPacket>(HttpMethod.Get, "users/$userId", UserPacket.serializer())

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion/"
    }
}
