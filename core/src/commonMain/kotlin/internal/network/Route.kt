package com.serebit.strife.internal.network

import com.serebit.strife.internal.packets.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Put
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
    val majorParameter: Long? = null,
    val requestPayload: RequestPayload = RequestPayload()
) {
    val uri get() = "$baseUri$path"

    // Channel Routes

    internal class GetChannel(channelID: Long) : Route<GenericChannelPacket>(
        Get, "/channels/$channelID", GenericChannelPacket.serializer(), channelID
    )

    internal class ModifyChannel(channelID: Long, outboundPacket: ModifyChannelPacket) : Route<GenericChannelPacket>(
        Patch, "/channels/$channelID", GenericChannelPacket.serializer(), channelID,
        RequestPayload(body = generateJsonBody(ModifyChannelPacket.serializer(), outboundPacket))
    )

    internal class DeleteChannel(channelID: Long) : Route<GenericChannelPacket>(
        Delete, "/channels/$channelID", GenericChannelPacket.serializer(), channelID
    )

    // Message Routes

    internal class GetChannelMessages(
        channelID: Long, outboundPacket: GetChannelMessagesPacket
    ) : Route<List<MessageCreatePacket>>(
        Get, "/channels/$channelID/messages", MessageCreatePacket.serializer().list, channelID,
        RequestPayload(body = generateJsonBody(GetChannelMessagesPacket.serializer(), outboundPacket))
    )

    internal class GetChannelMessage(channelID: Long, messageID: Long) : Route<MessageCreatePacket>(
        Get, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(), channelID
    )

    internal class CreateMessage(channelID: Long, packet: MessageSendPacket) : Route<MessageCreatePacket>(
        HttpMethod.Post, "/channels/$channelID/messages", MessageCreatePacket.serializer(), channelID,
        RequestPayload(body = generateJsonBody(MessageSendPacket.serializer(), packet))
    )

    internal class EditMessage(channelID: Long, messageID: Long, packet: MessageEditPacket) :
        Route<MessageCreatePacket>(
            HttpMethod.Patch, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
            channelID, RequestPayload(body = generateJsonBody(MessageEditPacket.serializer(), packet))
        )

    internal class DeleteMessage(channelID: Long, messageID: Long) : Route<Unit>(
        Delete, "/channels/$channelID/messages/$messageID", majorParameter = channelID
    )

    // Gateway Routes

    internal object GetGatewayBot : Route<Unit>(Get, "/gateway/bot")

    // Guild Routes

    internal class GetGuild(guildID: Long) : Route<GuildCreatePacket>(
        Get, "/guilds/$guildID", GuildCreatePacket.serializer(), guildID
    )

    internal class KickMember(guildID: Long, userID: Long) : Route<Unit>(
        Delete, "/guilds/$guildID/members/$userID", majorParameter = guildID
    )

    internal class BanMember(guildID: Long, userID: Long, deleteMessageDays: Int, reason: String) : Route<Unit>(
        Put, "/guilds/$guildID/bans/$userID", majorParameter = guildID,
        requestPayload = RequestPayload(
            parameters = mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        )
    )

    // User Routes

    internal object GetCurrentUser : Route<UserPacket>(Get, "/users/@me", UserPacket.serializer())

    internal class GetUser(userID: Long) : Route<UserPacket>(Get, "/users/$userID", UserPacket.serializer())

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
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
