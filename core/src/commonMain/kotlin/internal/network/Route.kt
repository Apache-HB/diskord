package com.serebit.strife.internal.network

import com.serebit.strife.internal.packets.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.map

internal sealed class Route<R>(
    val method: HttpMethod,
    private val path: String,
    val serializer: KSerializer<R>? = null,
    val requestPayload: RequestPayload = RequestPayload(),
    val majorParameters: List<Long> = emptyList()
) {
    val uri get() = "$baseUri$path"

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion/"
        private val json = Json(encodeDefaults = false)

        internal fun <T : Any> generateJsonBody(serializer: KSerializer<T>, data: T) = TextContent(
            json.stringify(serializer, data),
            ContentType.parse("application/json")
        )

        internal fun generateJsonBody(data: Map<String, String>) = TextContent(
            json.stringify((StringSerializer to StringSerializer).map, data),
            ContentType.parse("application/json")
        )

        internal fun generateStringBody(text: String) = TextContent(text, ContentType.Any)
    }
}

internal sealed class ChannelRoute<R>(
    method: HttpMethod,
    path: String,
    serializer: KSerializer<R>,
    channelID: Long
) : Route<R>(method, path, serializer, majorParameters = listOf(channelID)) {
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
    guildID: Long? = null,
    serializer: KSerializer<R>? = null,
    payload: RequestPayload = RequestPayload()
) : Route<R>(method, path, serializer, payload, majorParameters = guildID?.let { listOf(it) } ?: emptyList()) {
    class Get(guildID: Long) : GuildRoute<GuildCreatePacket>(
        HttpMethod.Get, "guilds/$guildID", guildID,
        GuildCreatePacket.serializer()
    )

    object Create : GuildRoute<GuildCreatePacket>(
        HttpMethod.Post, "guilds", serializer = GuildCreatePacket.serializer()
    )

    class CreateRole(guildID: Long) : GuildRoute<RolePacket>(
        HttpMethod.Post, "guilds/$guildID/roles", guildID,
        RolePacket.serializer()
    )

    class KickMember(guildID: Long, userID: Long) : GuildRoute<Unit>(
        HttpMethod.Delete, "guilds/$guildID/members/$userID", guildID
    )

    class BanMember(guildID: Long, userID: Long, deleteMessageDays: Int, reason: String) : GuildRoute<Unit>(
        HttpMethod.Put, "guilds/$guildID/bans/$userID", guildID,
        payload = RequestPayload(
            parameters = mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        )
    )

    class CreateChannel(guildID: Long) : GuildRoute<GenericChannelPacket>(
        HttpMethod.Post, "guilds/$guildID/channels", guildID, GenericChannelPacket.serializer()
    )
}

internal sealed class MessageRoute<R>(
    method: HttpMethod,
    path: String,
    channelID: Long,
    serializer: KSerializer<R>? = null,
    payload: RequestPayload = RequestPayload()
) : Route<R>(method, path, serializer, payload, listOf(channelID)) {
    internal class GetMultiple(channelID: Long) : MessageRoute<List<MessageCreatePacket>>(
        HttpMethod.Get, "channels/$channelID/messages", channelID, MessageCreatePacket.serializer().list
    )

    internal class Get(channelID: Long, messageID: Long) : MessageRoute<MessageCreatePacket>(
        HttpMethod.Get, "channels/$channelID/messages/$messageID", channelID, MessageCreatePacket.serializer()
    )

    internal class Create(channelID: Long, text: String) : MessageRoute<MessageCreatePacket>(
        HttpMethod.Post, "channels/$channelID/messages", channelID, MessageCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(mapOf("content" to text)))
    )

    internal class Edit(channelID: Long, messageID: Long, text: String) : MessageRoute<MessageCreatePacket>(
        HttpMethod.Patch, "channels/$channelID/messages/$messageID", channelID, MessageCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(mapOf("content" to text)))
    )

    internal class Delete(channelID: Long, messageID: Long) : MessageRoute<Unit>(
        HttpMethod.Delete, "channels/$channelID/messages/$messageID", channelID
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
