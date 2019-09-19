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

    class GetChannel(channelID: Long) : Route<ChannelPacket>(
        Get, "/channels/$channelID", ChannelPacket.polymorphicSerializer
    )

    class ModifyChannel(channelID: Long, packet: ModifyChannelPacket) : Route<ChannelPacket>(
        Patch, "/channels/$channelID", ChannelPacket.polymorphicSerializer,
        RequestPayload(body = generateJsonBody(ModifyChannelPacket.serializer(), packet))
    )

    class DeleteChannel(channelID: Long) : Route<ChannelPacket>(
        Delete, "/channels/$channelID", ChannelPacket.polymorphicSerializer
    )

    class EditChannelPermissions(channelID: Long, override: PermissionOverride) : Route<Nothing>(
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

    class DeleteChannelPermission(channelID: Long, overrideID: Long) : Route<Nothing>(
        Delete, "/channels/$channelID/permissions/$overrideID",
        ratelimitPath = "/channels/$channelID/permissions/overrideID"
    )

    class TriggerTypingIndicator(channelID: Long) : Route<Nothing>(
        Post, "/channels/$channelID/typing",
        requestPayload = RequestPayload(body = TextContent("", ContentType.Any))
    )

    class GetPinnedMessages(channelID: Long) : Route<List<MessageCreatePacket>>(
        Get, "/channels/$channelID/pins", MessageCreatePacket.serializer().list
    )

    class AddPinnedChannelMessage(channelID: Long, messageID: Long) : Route<Nothing>(
        Put, "/channels/$channelID/pins/$messageID", ratelimitPath = "/channels/$channelID/pins/messageID"
    )

    class DeletePinnedChannelMessage(channelID: Long, messageID: Long) : Route<Nothing>(
        Delete, "/channels/$channelID/pins/$messageID", ratelimitPath = "/channels/$channelID/pins/messageID"
    )

    // Message Routes

    class GetChannelMessages(
        channelID: Long,
        around: Long? = null,
        before: Long? = null,
        after: Long? = null,
        limit: Int? = null
    ) : Route<List<MessageCreatePacket>>(
        Get, "/channels/$channelID/messages", MessageCreatePacket.serializer().list,
        RequestPayload(parameters = mapOf(
            "around" to around.toString(),
            "before" to before.toString(),
            "after" to after.toString(),
            "limit" to limit.toString()
        ).filterValues { it != "null" })
    )

    class GetChannelMessage(channelID: Long, messageID: Long) : Route<MessageCreatePacket>(
        Get, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
        ratelimitPath = "/channels/$channelID/messages/messageID"
    )

    class CreateMessage(channelID: Long, packet: MessageSendPacket) : Route<MessageCreatePacket>(
        Post, "/channels/$channelID/messages", MessageCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(MessageSendPacket.serializer(), packet))
    )

    class CreateReaction(channelID: Long, messageID: Long, emoji: Emoji) : Route<Nothing>(
        Put, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData}/@me",
        requestPayload = RequestPayload(body = generateStringBody(emoji.requestData)),
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
    )

    class DeleteOwnReaction(channelID: Long, messageID: Long, emoji: Emoji) : Route<Nothing>(
        Delete, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData}/@me",
        requestPayload = RequestPayload(body = generateStringBody(emoji.requestData)),
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
    )

    class DeleteUserReaction(channelID: Long, messageID: Long, emoji: Emoji, userID: Long) : Route<Nothing>(
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

    class DeleteAllReactions(channelID: Long, messageID: Long) : Route<Nothing>(
        Delete, "/channels/$channelID/messages/$messageID/reactions",
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions"
    )

    class EditMessage(channelID: Long, messageID: Long, packet: MessageEditPacket) :
        Route<MessageCreatePacket>(
            Patch, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
            RequestPayload(body = generateJsonBody(MessageEditPacket.serializer(), packet)),
            "/channels/$channelID/messages/messageID"
        )

    class DeleteMessage(channelID: Long, messageID: Long) : Route<Nothing>(
        Delete, "/channels/$channelID/messages/$messageID",
        // this is formatted differently due to Discord's policy for rate limiting message deletion by bots
        ratelimitPath = "/channels/$channelID/messages/messageID?delete"
    )

    class BulkDeleteMessages(channelID: Long, messages: List<Long>) : Route<Nothing>(
        Post, "/channels/$channelID/messages/bulk-delete", requestPayload = RequestPayload(
            body = generateJsonBody(BulkDeleteMessagesPacket.serializer(), BulkDeleteMessagesPacket(messages))
        )
    )

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

    class DeleteGuildEmoji(guildID: Long, emojiID: Long) : Route<Nothing>(
        Delete, "/guilds/$guildID/emojis/$emojiID", ratelimitPath = "/guilds/$guildID/emojis/emojiID"
    )

    // Guild Routes

    class CreateGuild(packet: CreateGuildPacket) : Route<Nothing>(
        Post, "/guilds",
        requestPayload = RequestPayload(body = generateJsonBody(CreateGuildPacket.serializer(), packet))
    )

    class GetGuild(guildID: Long) : Route<GuildCreatePacket>(
        Get, "/guilds/$guildID", GuildCreatePacket.serializer()
    )

    class ModifyGuild(guildID: Long, packet: ModifyGuildPacket) : Route<GuildCreatePacket>(
        Patch, "/guilds/$guildID", GuildCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(ModifyGuildPacket.serializer(), packet))
    )

    class DeleteGuild(guildID: Long) : Route<Nothing>(Delete, "/guild/$guildID")

    class GetGuildChannels(guildID: Long) : Route<List<GuildChannelPacket>>(
        Get, "/guilds/$guildID/channels", GuildChannelPacket.polymorphicSerializer.list
    )

    class CreateGuildChannel(guildID: Long, packet: CreateGuildChannelPacket) : Route<GuildChannelPacket>(
        Post, "/guilds/$guildID/channels", GuildChannelPacket.polymorphicSerializer,
        RequestPayload(body = generateJsonBody(CreateGuildChannelPacket.serializer(), packet))
    )

    class ModifyGuildChannelPositions(guildID: Long, packet: List<ModifyGuildChannelPositionsPacket>) : Route<Nothing>(
        Patch, "/guilds/$guildID/channels", requestPayload =
        RequestPayload(body = generateJsonBody(ModifyGuildChannelPositionsPacket.serializer().list, packet))
    )

    class GetGuildMember(guildID: Long, userID: Long) : Route<GuildMemberPacket>(
        Get, "/guilds/$guildID/members/$userID", GuildMemberPacket.serializer(),
        ratelimitPath = "/guilds/$guildID/members/userID"
    )

    /**
     * TODO
     *
     * @constructor
     * TODO
     *
     * @param guildID
     * @param limit max number of members to return (1-1_000)
     * @param after
     */
    class ListGuildMembers(guildID: Long, limit: Int = 1, after: Long = 0) : Route<List<GuildMemberPacket>>(
        Get, "/guilds/$guildID/members", GuildMemberPacket.serializer().list,
        RequestPayload(mapOf("limit" to limit.toString(), "after" to after.toString()))
    )

    class AddGuildMember(guildID: Long, userID: Long, packet: AddGuildMemberPacket) : Route<GuildMemberPacket>(
        Put, "/guilds/$guildID/members/$userID", GuildMemberPacket.serializer(),
        RequestPayload(body = generateJsonBody(AddGuildMemberPacket.serializer(), packet))
    )

    class ModifyGuildMember(guildID: Long, userID: Long, packet: ModifyGuildMemberPacket) : Route<Nothing>(
        Patch, "/guilds/$guildID/members/$userID", requestPayload =
        RequestPayload(body = generateJsonBody(ModifyGuildMemberPacket.serializer(), packet))
    )

    class ModifyCurrentUserNick(guildID: Long, nickname: String) : Route<ModifyCurrentUserNickPacket>(
        Patch, "/guilds/$guildID/members/@me/nick", requestPayload = RequestPayload(
            body = generateJsonBody(
                ModifyCurrentUserNickPacket.serializer(),
                ModifyCurrentUserNickPacket(nickname)
            )
        )
    )

    class RemoveGuildMember(guildID: Long, userID: Long) : Route<Nothing>(
        Delete, "/guilds/$guildID/members/$userID", ratelimitPath = "/guilds/$guildID/members/userID"
    )

    class GetGuildBans(guildID: Long) : Route<List<BanPacket>>(
        Get, "/guilds/$guildID/bans", serializer = BanPacket.serializer().list
    )

    class GetGuildBan(guildID: Long, userID: Long) : Route<BanPacket>(
        Get, "/guilds/$guildID/bans/$userID", serializer = BanPacket.serializer(),
        ratelimitPath = "/guilds/$guildID/bans/userID"
    )

    class CreateGuildBan(guildID: Long, userID: Long, deleteMessageDays: Int, reason: String) : Route<Nothing>(
        Put, "/guilds/$guildID/bans/$userID", ratelimitPath = "/guilds/$guildID/members/userID",
        requestPayload = RequestPayload(
            parameters = mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        )
    )

    class RemoveGuildBan(guildID: Long, userID: Long) : Route<Nothing>(
        Delete, "/guilds/$guildID/bans/$userID", ratelimitPath = "/guilds/$guildID/bans/userID"
    )

    class GetGuildRoles(guildID: Long) : Route<List<GuildRolePacket>>(
        Get, "/guilds/$guildID/roles", serializer = GuildRolePacket.serializer().list
    )

    class CreateGuildRole(guildID: Long, role: CreateGuildRolePacket) : Route<GuildRolePacket>(
        Post, "/guilds/$guildID/roles", serializer = GuildRolePacket.serializer(),
        requestPayload = RequestPayload(body = generateJsonBody(CreateGuildRolePacket.serializer(), role))
    )

    class ModifyGuildRole(guildID: Long, roleID: Long, role: CreateGuildRolePacket) : Route<GuildRolePacket>(
        Patch, "/guilds/$guildID/roles/$roleID", serializer = GuildRolePacket.serializer(),
        requestPayload = RequestPayload(body = generateJsonBody(CreateGuildRolePacket.serializer(), role))
    )

    class DeleteGuildRole(guildID: Long, roleID: Long) : Route<Nothing>(
        Delete, "/guilds/$guildID/roles/$roleID", ratelimitPath = "/guilds/$guildID/roles/roleID"
    )

    class ModifyGuildRolePosition(guildID: Long, roleID: Long, position: Int) : Route<List<GuildRolePacket>>(
        Patch, "/guilds/$guildID/roles", serializer = GuildRolePacket.serializer().list,
        requestPayload = RequestPayload(
            body = generateJsonBody(
                ModifyGuildRolePositionPacket.serializer(),
                ModifyGuildRolePositionPacket(roleID, position)
            )
        )
    )

    class AddGuildMemberRole(guildID: Long, userID: Long, roleID: Long) :
        Route<Nothing>(Put, "/guilds/$guildID/members/$userID/roles/$roleID")

    class RemoveGuildMemberRole(guildID: Long, userID: Long, roleID: Long) :
        Route<Nothing>(Delete, "/guilds/$guildID/members/$userID/roles/$roleID")

    class GetGuildPruneCount(guildID: Long, days: Int = 7) : Route<PruneCountPacket>(
        Get, "/guilds/$guildID/prune", PruneCountPacket.serializer(), RequestPayload(mapOf("days" to "$days"))
    )

    /**
     * [See](https://discordapp.com/developers/docs/resources/guild#begin-guild-prune)
     *
     * @param days number of days to prune (1 or more)
     * @param computePruneCount whether 'pruned' is returned, discouraged for large guilds
     */
    class BeginGuildPrune(guildID: Long, days: Int = 7, computePruneCount: Boolean = true) : Route<PruneCountPacket>(
        Post, "/guilds/$guildID/prune", PruneCountPacket.serializer(),
        RequestPayload(mapOf("days" to "$days", "compute_prune_count" to "$computePruneCount"))
    )

    class GetGuildIntegrations(guildID: Long) : Route<List<GuildIntegrationPacket>>(
        Get, "/guilds/$guildID/integrations", GuildIntegrationPacket.serializer().list
    )

    class CreateGuildIntegration(guildID: Long, packet: CreateGuildIntegrationPacket) : Route<Nothing>(
        Post, "/guilds/$guildID/integrations",
        requestPayload = RequestPayload(body = generateJsonBody(CreateGuildIntegrationPacket.serializer(), packet))
    )

    class ModifyGuildIntegration(guildID: Long, integrationID: Long, packet: ModifyGuildIntegrationPacket) :
        Route<Nothing>(
            Patch, "/guilds/$guildID/integrations/$integrationID",
            requestPayload = RequestPayload(body = generateJsonBody(ModifyGuildIntegrationPacket.serializer(), packet))
        )

    class DeleteGuildIntegration(guildID: Long, integrationID: Long) :
        Route<Nothing>(Delete, "/guilds/$guildID/integrations/$integrationID")

    class SyncGuildIntegration(guildID: Long, integrationID: Long) :
        Route<Nothing>(Post, "/guilds/$guildID/integrations/$integrationID/sync")

    class GetGuildInvites(guildID: Long) : Route<List<InviteMetadataPacket>>(
        Get, "/guilds/$guildID/invites", InviteMetadataPacket.serializer().list
    )

    class GetGuildEmbed(guildID: Long) :
        Route<GuildEmbedPacket>(Get, "/guilds/$guildID/embed", GuildEmbedPacket.serializer())

    class ModifyGuildEmbed(guildID: Long, enable: Boolean = false, channelID: Long? = null) : Route<GuildEmbedPacket>(
        Patch, "/guilds/$guildID/embed", GuildEmbedPacket.serializer(),
        RequestPayload(body = generateJsonBody(GuildEmbedPacket.serializer(), GuildEmbedPacket(enable, channelID)))
    )

    class GetGuildVanityUrl(guildID: Long) :
        Route<PartialInvitePacket>(Get, "/guilds/$guildID/vanity-url", PartialInvitePacket.serializer())

    class GetGuildVoiceRegions(guildID: Long) : Route<List<VoiceRegionPacket>>(
        Get, "/guilds/$guildID/regions", VoiceRegionPacket.serializer().list
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

    class LeaveGuild(guildID: Long) : Route<Nothing>(
        Delete, "/users/@me/guilds/$guildID"
    )

    class CreateDM(recipientID: Long) : Route<DmChannelPacket>(
        Post, "/users/@me/channels", DmChannelPacket.serializer(),
        RequestPayload(body = generateJsonBody(CreateDMPacket.serializer(), CreateDMPacket(recipientID)))
    )

    // Gateway Routes & Misc

    object GetGatewayBot : Route<Nothing>(Get, "/gateway/bot")

    object ListVoiceRegions : Route<List<VoiceRegionPacket>>(Get, "/voice/regions", VoiceRegionPacket.serializer().list)

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
