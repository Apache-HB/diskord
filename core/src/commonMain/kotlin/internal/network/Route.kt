package com.serebit.strife.internal.network

import com.serebit.strife.data.MemberPermissionOverride
import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.RolePermissionOverride
import com.serebit.strife.data.toBitSet
import com.serebit.strife.internal.packets.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
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
    val requestPayload: RequestPayload = RequestPayload(),
    val ratelimitPath: String = path
) {
    val uri get() = "$baseUri$path"

    // Channel Routes

    internal class GetChannel(channelID: Long) : Route<GenericChannelPacket>(
        Get, "/channels/$channelID", GenericChannelPacket.serializer()
    )

    internal class ModifyChannel(channelID: Long, outboundPacket: ModifyChannelPacket) : Route<GenericChannelPacket>(
        Patch, "/channels/$channelID", GenericChannelPacket.serializer(),
        RequestPayload(body = generateJsonBody(ModifyChannelPacket.serializer(), outboundPacket))
    )

    internal class DeleteChannel(channelID: Long) : Route<GenericChannelPacket>(
        Delete, "/channels/$channelID", GenericChannelPacket.serializer()
    )

    internal class TriggerTypingIndicator(channelID: Long) : Route<Unit>(
        Post, "/channels/$channelID/typing", null,
        RequestPayload(body = TextContent("", ContentType.Application.Any))
    )

    internal class EditChannelPermissions(channelID: Long, override: PermissionOverride) : Route<Unit>(
        Put, "/channels/$channelID/permissions/${override.id}",
        requestPayload = RequestPayload(
            mapOf(
                "allow" to override.allow.toBitSet().toString(),
                "deny" to override.deny.toBitSet().toString(),
                "type" to when (override) {
                    is RolePermissionOverride -> "role"
                    is MemberPermissionOverride -> "member"
                }
            )
        ), ratelimitPath = "/channels/$channelID/permissions/overrideID"
    )

    // Message Routes

    internal class GetChannelMessages(
        channelID: Long, outboundPacket: GetChannelMessagesPacket
    ) : Route<List<MessageCreatePacket>>(
        Get, "/channels/$channelID/messages", MessageCreatePacket.serializer().list,
        RequestPayload(body = generateJsonBody(GetChannelMessagesPacket.serializer(), outboundPacket))
    )

    internal class GetChannelMessage(channelID: Long, messageID: Long) : Route<MessageCreatePacket>(
        Get, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
        ratelimitPath = "/channels/$channelID/messages/messageID"
    )

    internal class CreateMessage(channelID: Long, packet: MessageSendPacket) : Route<MessageCreatePacket>(
        HttpMethod.Post, "/channels/$channelID/messages", MessageCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(MessageSendPacket.serializer(), packet))
    )

    internal class EditMessage(channelID: Long, messageID: Long, packet: MessageEditPacket) :
        Route<MessageCreatePacket>(
            HttpMethod.Patch, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
            RequestPayload(body = generateJsonBody(MessageEditPacket.serializer(), packet)),
            "/channels/$channelID/messages/messageID"
        )

    internal class DeleteMessage(channelID: Long, messageID: Long) : Route<Unit>(
        Delete, "/channels/$channelID/messages/$messageID",
        // this is formatted differently due to Discord's policy for rate limiting message deletion by bots
        ratelimitPath = "/channels/$channelID/messages/messageID?delete"
    )

    // Gateway Routes

    internal object GetGatewayBot : Route<Unit>(Get, "/gateway/bot")

    // Guild Routes

    internal class GetGuild(guildID: Long) : Route<GuildCreatePacket>(
        Get, "/guilds/$guildID", GuildCreatePacket.serializer()
    )

    internal class KickMember(guildID: Long, userID: Long) : Route<Unit>(
        Delete, "/guilds/$guildID/members/$userID", ratelimitPath = "/guilds/$guildID/members/userID"
    )

    internal class BanMember(guildID: Long, userID: Long, deleteMessageDays: Int, reason: String) : Route<Unit>(
        Put, "/guilds/$guildID/bans/$userID", ratelimitPath = "/guilds/$guildID/members/userID",
        requestPayload = RequestPayload(
            parameters = mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        )
    )

    // User Routes

    internal object GetCurrentUser : Route<UserPacket>(Get, "/users/@me", UserPacket.serializer())

    internal class GetUser(userID: Long) : Route<UserPacket>(
        Get, "/users/$userID", UserPacket.serializer(), ratelimitPath = "/users/userID"
    )

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
