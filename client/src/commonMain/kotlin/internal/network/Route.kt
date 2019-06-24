package com.serebit.strife.internal.network

import com.serebit.strife.data.*
import com.serebit.strife.entities.Emoji
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
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.map

internal sealed class Route<R : Any>(
    val method: HttpMethod,
    private val path: String,
    val serializer: KSerializer<R>? = null,
    val requestPayload: RequestPayload = RequestPayload(),
    val ratelimitPath: String = path
) {
    val uri get() = "$baseUri$path"

    // Channel Routes

    class GetChannel(channelID: Long) : Route<GenericChannelPacket>(
        Get, "/channels/$channelID", GenericChannelPacket.serializer()
    )

    class ModifyChannel(channelID: Long, packet: ModifyChannelPacket) : Route<GenericChannelPacket>(
        Patch, "/channels/$channelID", GenericChannelPacket.serializer(),
        RequestPayload(body = generateJsonBody(ModifyChannelPacket.serializer(), packet))
    )

    class DeleteChannel(channelID: Long) : Route<GenericChannelPacket>(
        Delete, "/channels/$channelID", GenericChannelPacket.serializer()
    )

    class EditChannelPermissions(channelID: Long, override: PermissionOverride) : Route<Unit>(
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

    class GetChannelInvites(channelID: Long) : Route<List<InviteMetadataPacket>>(
        Get, "/channels/$channelID/invites", InviteMetadataPacket.serializer().list
    )

    class CreateChannelInvite(channelID: Long, packet: CreateChannelInvitePacket) : Route<InvitePacket>(
        Post, "/channels/$channelID/invites", InvitePacket.serializer(),
        RequestPayload(body = generateJsonBody(CreateChannelInvitePacket.serializer(), packet))
    )

    class DeleteChannelPermission(channelID: Long, overrideID: Long) : Route<Unit>(
        Delete, "/channels/$channelID/permissions/$overrideID",
        ratelimitPath = "/channels/$channelID/permissions/overrideID"
    )

    class TriggerTypingIndicator(channelID: Long) : Route<Unit>(
        Post, "/channels/$channelID/typing",
        requestPayload = RequestPayload(body = TextContent("", ContentType.Any))
    )

    class GetPinnedMessages(channelID: Long) : Route<List<MessageCreatePacket>>(
        Get, "/channels/$channelID/pins", MessageCreatePacket.serializer().list
    )

    class AddPinnedChannelMessage(channelID: Long, messageID: Long) : Route<Unit>(
        Put, "/channels/$channelID/pins/$messageID", ratelimitPath = "/channels/$channelID/pins/messageID"
    )

    class DeletePinnedChannelMessage(channelID: Long, messageID: Long) : Route<Unit>(
        Delete, "/channels/$channelID/pins/$messageID", ratelimitPath = "/channels/$channelID/pins/messageID"
    )

    // Message Routes

    class GetChannelMessages(
        channelID: Long, packet: GetChannelMessagesPacket
    ) : Route<List<MessageCreatePacket>>(
        Get, "/channels/$channelID/messages", MessageCreatePacket.serializer().list,
        RequestPayload(body = generateJsonBody(GetChannelMessagesPacket.serializer(), packet))
    )

    class GetChannelMessage(channelID: Long, messageID: Long) : Route<MessageCreatePacket>(
        Get, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
        ratelimitPath = "/channels/$channelID/messages/messageID"
    )

    class CreateMessage(channelID: Long, packet: MessageSendPacket) : Route<MessageCreatePacket>(
        Post, "/channels/$channelID/messages", MessageCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(MessageSendPacket.serializer(), packet))
    )

    class CreateReaction(channelID: Long, messageID: Long, emoji: Emoji) : Route<Unit>(
        Put, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData}/@me",
        requestPayload = RequestPayload(body = generateStringBody(emoji.requestData)),
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
    )

    class DeleteOwnReaction(channelID: Long, messageID: Long, emoji: Emoji) : Route<Unit>(
        Delete, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData}/@me",
        requestPayload = RequestPayload(body = generateStringBody(emoji.requestData)),
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
    )

    class DeleteUserReaction(channelID: Long, messageID: Long, emoji: Emoji, userID: Long) : Route<Unit>(
        Delete, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData}/$userID",
        requestPayload = RequestPayload(body = generateStringBody(emoji.requestData)),
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/userID"
    )

    class GetReactions(
        channelID: Long, messageID: Long, emoji: Emoji, packet: GetReactionsPacket
    ) : Route<List<UserPacket>>(
        Get, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData}", UserPacket.serializer().list,
        RequestPayload(body = Companion.generateJsonBody(GetReactionsPacket.serializer(), packet)),
        "/channels/$channelID/messages/messageID/reactions/emoji"
    )

    class DeleteAllReactions(channelID: Long, messageID: Long) : Route<Unit>(
        Delete, "/channels/$channelID/messages/$messageID/reactions",
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions"
    )

    class EditMessage(channelID: Long, messageID: Long, packet: MessageEditPacket) :
        Route<MessageCreatePacket>(
            Patch, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
            RequestPayload(body = generateJsonBody(MessageEditPacket.serializer(), packet)),
            "/channels/$channelID/messages/messageID"
        )

    class DeleteMessage(channelID: Long, messageID: Long) : Route<Unit>(
        Delete, "/channels/$channelID/messages/$messageID",
        // this is formatted differently due to Discord's policy for rate limiting message deletion by bots
        ratelimitPath = "/channels/$channelID/messages/messageID?delete"
    )

    class BulkDeleteMessages(channelID: Long, messages: List<Long>) : Route<Unit>(
        Post, "/channels/$channelID/messages/bulk-delete", requestPayload = RequestPayload(
            body = generateJsonBody(BulkDeleteMessagesPacket.serializer(), BulkDeleteMessagesPacket(messages))
        )
    )

    // Gateway Routes

    object GetGatewayBot : Route<Unit>(Get, "/gateway/bot")

    // Emoji Routes

    class ListGuildEmojis(guildID: Long) : Route<List<GuildEmojiPacket>>(
        Get, "/guilds/$guildID/emojis", GuildEmojiPacket.serializer().list
    )

    class GetGuildEmoji(guildID: Long, emojiID: Long) : Route<GuildEmojiPacket>(
        Get, "/guilds/$guildID/emojis/$emojiID", GuildEmojiPacket.serializer(),
        ratelimitPath = "/guilds/$guildID/emojis/emojiID"
    )

    class CreateGuildEmoji(guildID: Long, packet: CreateGuildEmojiPacket) : Route<GuildEmojiPacket>(
        Post, "/guilds/$guildID/emojis", GuildEmojiPacket.serializer(),
        RequestPayload(body = generateJsonBody(CreateGuildEmojiPacket.serializer(), packet))
    )

    class ModifyGuildEmoji(guildID: Long, emojiID: Long, packet: ModifyGuildEmojiPacket) : Route<GuildEmojiPacket>(
        Patch, "/guilds/$guildID/emojis/$emojiID", GuildEmojiPacket.serializer(),
        RequestPayload(body = generateJsonBody(ModifyGuildEmojiPacket.serializer(), packet)),
        "/guilds/$guildID/emojis/emojiID"
    )

    class DeleteGuildEmoji(guildID: Long, emojiID: Long) : Route<Unit>(
        Delete, "/guilds/$guildID/emojis/$emojiID", ratelimitPath = "/guilds/$guildID/emojis/emojiID"
    )

    // Guild Routes

    class GetGuild(guildID: Long) : Route<GuildCreatePacket>(
        Get, "/guilds/$guildID", GuildCreatePacket.serializer()
    )

    class ModifyGuild(guildID: Long, packet: ModifyGuildPacket) : Route<GuildCreatePacket>(
        Patch, "/guilds/$guildID", GuildCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(ModifyGuildPacket.serializer(), packet))
    )

    class GetGuildChannels(guildID: Long) : Route<List<GenericGuildChannelPacket>>(
        Get, "/guilds/$guildID/channels", GenericGuildChannelPacket.serializer().list
    )

    class CreateGuildChannel(guildID: Long, packet: CreateGuildChannelPacket) : Route<GenericGuildChannelPacket>(
        Post, "/guilds/$guildID/channels", GenericGuildChannelPacket.serializer(),
        RequestPayload(body = generateJsonBody(CreateGuildChannelPacket.serializer(), packet))
    )

    class ModifyGuildChannelPositions(guildID: Long, packet: List<ModifyGuildChannelPositionsPacket>) : Route<Unit>(
        Patch, "/guilds/$guildID/channels", requestPayload =
        RequestPayload(body = generateJsonBody(ModifyGuildChannelPositionsPacket.serializer().list, packet))
    )

    class GetGuildMember(guildID: Long, userID: Long) : Route<GuildMemberPacket>(
        Get, "/guilds/$guildID/members/$userID", GuildMemberPacket.serializer(),
        ratelimitPath = "/guilds/$guildID/members/userID"
    )

    class RemoveGuildMember(guildID: Long, userID: Long) : Route<Unit>(
        Delete, "/guilds/$guildID/members/$userID", ratelimitPath = "/guilds/$guildID/members/userID"
    )

    class GetGuildBans(guildID: Long) : Route<List<BanPacket>>(
        Get, "/guilds/$guildID/bans", serializer = BanPacket.serializer().list
    )

    class GetGuildBan(guildID: Long, userID: Long) : Route<BanPacket>(
        Get, "/guilds/$guildID/bans/$userID", serializer = BanPacket.serializer(),
        ratelimitPath = "/guilds/$guildID/bans/userID"
    )

    class CreateGuildBan(guildID: Long, userID: Long, deleteMessageDays: Int, reason: String) : Route<Unit>(
        Put, "/guilds/$guildID/bans/$userID", ratelimitPath = "/guilds/$guildID/members/userID",
        requestPayload = RequestPayload(
            parameters = mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        )
    )

    class RemoveGuildBan(guildID: Long, userID: Long) : Route<Unit>(
        Delete, "/guilds/$guildID/bans/$userID", ratelimitPath = "/guilds/$guildID/bans/userID"
    )

    class GetGuildRoles(guildID: Long) : Route<List<GuildRolePacket>>(
        Get, "/guilds/$guildID/roles", serializer = GuildRolePacket.serializer().list
    )

    class DeleteGuildRole(guildID: Long, roleID: Long) : Route<Unit>(
        Delete, "/guilds/$guildID/roles/$roleID", ratelimitPath = "/guilds/$guildID/roles/roleID"
    )

    class GetGuildInvites(guildID: Long) : Route<List<InviteMetadataPacket>>(
        Get, "/guilds/$guildID/invites", InviteMetadataPacket.serializer().list
    )

    // Invite Routes

    class GetInvite(inviteCode: String, withCounts: Boolean) : Route<InvitePacket>(
        Get, "/invites/$inviteCode", InvitePacket.serializer(),
        RequestPayload(mapOf("with_counts" to withCounts.toString())), "/invites/inviteCode"
    )

    class DeleteInvite(inviteCode: String) : Route<InvitePacket>(
        Delete, "/invites/$inviteCode", InvitePacket.serializer(), ratelimitPath = "/invites/inviteCode"
    )

    // User Routes

    object GetCurrentUser : Route<UserPacket>(Get, "/users/@me", UserPacket.serializer())

    class GetUser(userID: Long) : Route<UserPacket>(
        Get, "/users/$userID", UserPacket.serializer(), ratelimitPath = "/users/userID"
    )

    class ModifyCurrentUser(username: String?, avatarData: AvatarData?) : Route<UserPacket>(
        Patch, "/users/@me", UserPacket.serializer(),
        RequestPayload(
            body = generateJsonBody(
                ModifyCurrentUserPacket.serializer(),
                ModifyCurrentUserPacket(username, avatarData?.dataUri)
            )
        )
    )

    class GetCurrentUserGuilds(before: Long? = null, after: Long? = null, limit: Int = 100) :
        Route<List<PartialGuildPacket>>(
            Get, "/users/@me/guilds", PartialGuildPacket.serializer().list,
            RequestPayload(
                listOfNotNull(
                    before?.toString()?.let { "before" to it },
                    after?.toString()?.let { "after" to it },
                    "limit" to limit.toString()
                ).toMap()
            )
        )

    class LeaveGuild(guildID: Long) : Route<Unit>(
        Delete, "/users/@me/guilds/$guildID"
    )

    class CreateDM(recipientID: Long) : Route<DmChannelPacket>(
        Post, "/users/@me/channels", DmChannelPacket.serializer(),
        RequestPayload(body = generateJsonBody(CreateDMPacket.serializer(), CreateDMPacket(recipientID)))
    )

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
        @UseExperimental(UnstableDefault::class)
        private val json = Json(JsonConfiguration(encodeDefaults = false))

        fun <T : Any> generateJsonBody(serializer: KSerializer<T>, data: T) = TextContent(
            json.stringify(serializer, data),
            ContentType.Application.Json
        )

        fun generateJsonBody(data: Map<String, String>) = TextContent(
            json.stringify((StringSerializer to StringSerializer).map, data),
            ContentType.Application.Json
        )

        fun generateStringBody(text: String) = TextContent(text, ContentType.Text.Plain)
    }
}

internal expect fun encodeBase64(bytes: ByteArray): String
