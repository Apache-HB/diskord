package com.serebit.strife.internal.network

import com.serebit.strife.internal.packets.*
import io.ktor.http.HttpMethod
import kotlinx.serialization.KSerializer
import kotlinx.serialization.list

internal sealed class Route<R>(
    val method: HttpMethod,
    private val path: String,
    val serializer: KSerializer<R>? = null,
    val uriParameters: Map<String, String> = emptyMap(),
    val majorParameters: List<Long> = emptyList()
) {
    val uri get() = "$baseUri$path"

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion/"
    }
}

internal sealed class ChannelRoute<R>(
    method: HttpMethod,
    path: String,
    serializer: KSerializer<R>,
    channelID: Long
) : Route<R>(method, path, serializer, emptyMap(), listOf(channelID)) {
    class Get(channelID: Long) : ChannelRoute<GenericChannelPacket>(
        HttpMethod.Get, "channels/$channelID", GenericChannelPacket.serializer(),
        channelID
    )

    class GetAsText(channelID: Long) : ChannelRoute<GenericTextChannelPacket>(
        HttpMethod.Get, "channels/$channelID", GenericTextChannelPacket.serializer(),
        channelID
    )

    class GetAsDM(channelID: Long) : ChannelRoute<DmChannelPacket>(
        HttpMethod.Get, "channels/$channelID", DmChannelPacket.serializer(),
        channelID
    )

    class GetAsGuildText(channelID: Long) : ChannelRoute<GuildTextChannelPacket>(
        HttpMethod.Get, "channels/$channelID", GuildTextChannelPacket.serializer(),
        channelID
    )

    class GetAsGuildVoice(channelID: Long) : ChannelRoute<GuildVoiceChannelPacket>(
        HttpMethod.Get, "channels/$channelID", GuildVoiceChannelPacket.serializer(),
        channelID
    )

    class GetAsGuildCategory(channelID: Long) : ChannelRoute<GuildChannelCategoryPacket>(
        HttpMethod.Get, "channels/$channelID", GuildChannelCategoryPacket.serializer(),
        channelID
    )
}

internal sealed class GatewayRoute(path: String) : Route<Unit>(HttpMethod.Get, path) {
    object Get : GatewayRoute("gateway")

    object GetBot : GatewayRoute("gateway/bot")
}

internal sealed class GuildRoute<R>(
    method: HttpMethod,
    path: String,
    serializer: KSerializer<R>?,
    guildID: Long? = null
) : Route<R>(method, path, serializer, emptyMap(), guildID?.let { listOf(it) } ?: emptyList()) {
    class Get(guildID: Long) : GuildRoute<GuildCreatePacket>(
        HttpMethod.Get, "guilds/$guildID", GuildCreatePacket.serializer(),
        guildID
    )

    object Create : GuildRoute<GuildCreatePacket>(
        HttpMethod.Post, "guilds", GuildCreatePacket.serializer()
    )

    class CreateRole(guildID: Long) : GuildRoute<RolePacket>(
        HttpMethod.Post, "guilds/$guildID/roles", RolePacket.serializer(),
        guildID
    )

    class KickMember(guildID: Long, userID: Long) : GuildRoute<Unit>(
        HttpMethod.Delete, "guilds/$guildID/members/$userID", null,
        guildID
    )

    class BanMember(guildID: Long, userID: Long) : GuildRoute<Unit>(
        HttpMethod.Put, "guilds/$guildID/bans/$userID", null,
        guildID
    )

    class CreateChannel(guildID: Long) : GuildRoute<GenericChannelPacket>(
        HttpMethod.Post, "guilds/$guildID/channels", GenericChannelPacket.serializer(),
        guildID
    )
}

internal sealed class MessageRoute<R>(
    method: HttpMethod,
    path: String,
    serializer: KSerializer<R>?,
    channelID: Long
) : Route<R>(method, path, serializer, emptyMap(), listOf(channelID)) {
    constructor(method: HttpMethod, path: String, channelID: Long) : this(method, path, null, channelID)

    internal class GetMultiple(channelID: Long) : MessageRoute<List<MessageCreatePacket>>(
        HttpMethod.Get, "channels/$channelID/messages", MessageCreatePacket.serializer().list,
        channelID
    )

    internal class Get(channelID: Long, messageID: Long) : MessageRoute<MessageCreatePacket>(
        HttpMethod.Get, "channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
        channelID
    )

    internal class Create(channelID: Long) : MessageRoute<MessageCreatePacket>(
        HttpMethod.Post, "channels/$channelID/messages", MessageCreatePacket.serializer(),
        channelID
    )

    internal class Edit(channelID: Long, messageID: Long) : MessageRoute<MessageCreatePacket>(
        HttpMethod.Patch, "channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
        channelID
    )

    internal class Delete(channelID: Long, messageID: Long) : MessageRoute<Unit>(
        HttpMethod.Delete, "channels/$channelID/messages/$messageID",
        channelID
    )
}

internal sealed class UserRoute<R>(
    method: HttpMethod,
    path: String,
    serializer: KSerializer<R>
) : Route<R>(method, path, serializer) {
    object GetSelf : UserRoute<UserPacket>(HttpMethod.Get, "users/@me", UserPacket.serializer())

    class Get(userID: Long) : UserRoute<UserPacket>(HttpMethod.Get, "users/$userID", UserPacket.serializer())

    object CreateDMChannel : UserRoute<DmChannelPacket>(
        HttpMethod.Post, "users/@me/channels", DmChannelPacket.serializer()
    )
}
