package com.serebit.strife.internal.network

import com.serebit.strife.data.*
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

    class ModifyChannel(
        channelID: Long,
        name: String? = null,
        position: Int? = null,
        topic: String? = null,
        nsfw: Boolean? = null,
        rate_limit_per_user: Int? = null,
        bitrate: Int? = null,
        user_limit: Int? = null,
        permission_overwrites: List<PermissionOverwritePacket>? = null,
        parent_id: Long? = null
    ) : Route<ChannelPacket>(
        Patch, "/channels/$channelID", ChannelPacket.polymorphicSerializer,
        RequestPayload(
            body = generateJsonBody(
                ModifyChannelPacket.serializer(), ModifyChannelPacket(
                    name,
                    position,
                    topic,
                    nsfw,
                    rate_limit_per_user,
                    bitrate,
                    user_limit,
                    permission_overwrites,
                    parent_id
                )
            )
        )
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

    class CreateChannelInvite(
        channelID: Long,
        max_age: Int = 86400,
        max_uses: Int = 0,
        temporary: Boolean = false,
        unique: Boolean = false
    ) : Route<InvitePacket>(
        Post, "/channels/$channelID/invites", InvitePacket.serializer(),
        RequestPayload(
            body = generateJsonBody(
                CreateChannelInvitePacket.serializer(),
                CreateChannelInvitePacket(max_age, max_uses, temporary, unique)
            )
        )
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

    class CreateMessage(
        channelID: Long,
        content: String? = null,
        tts: Boolean? = null,
        embed: OutgoingEmbedPacket? = null
    ) : Route<MessageCreatePacket>(
        Post, "/channels/$channelID/messages", MessageCreatePacket.serializer(),
        RequestPayload(body = generateJsonBody(MessageSendPacket.serializer(), MessageSendPacket(content, tts, embed)))
    )

    class CreateReaction(channelID: Long, messageID: Long, uriData: String, requestData: String) : Route<Nothing>(
        Put, "/channels/$channelID/messages/$messageID/reactions/$uriData/@me",
        requestPayload = RequestPayload(body = generateStringBody(requestData)),
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
    )

    class DeleteOwnReaction(channelID: Long, messageID: Long, uriData: String, requestData: String) : Route<Nothing>(
        Delete, "/channels/$channelID/messages/$messageID/reactions/$uriData}/@me",
        requestPayload = RequestPayload(body = generateStringBody(requestData)),
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
    )

    class DeleteUserReaction(channelID: Long, messageID: Long, uriData: String, requestData: String, userID: Long) :
        Route<Nothing>(
            Delete, "/channels/$channelID/messages/$messageID/reactions/$uriData/$userID",
            requestPayload = RequestPayload(body = generateStringBody(requestData)),
            ratelimitPath = "/channels/$channelID/messages/messageID/reactions/emoji/userID"
        )

    class GetReactions(
        channelID: Long,
        messageID: Long,
        emojiUriData: String,
        before: Long? = null,
        after: Long? = null,
        limit: Int = 25
    ) : Route<List<UserPacket>>(
        Get, "/channels/$channelID/messages/$messageID/reactions/$emojiUriData", UserPacket.serializer().list,
        RequestPayload(
            body = Companion.generateJsonBody(
                GetReactionsPacket.serializer(), GetReactionsPacket(before, after, limit)
            )
        ),
        "/channels/$channelID/messages/messageID/reactions/emoji"
    )

    class DeleteAllReactions(channelID: Long, messageID: Long) : Route<Nothing>(
        Delete, "/channels/$channelID/messages/$messageID/reactions",
        ratelimitPath = "/channels/$channelID/messages/messageID/reactions"
    )

    class EditMessage(channelID: Long, messageID: Long, content: String? = null, embed: OutgoingEmbedPacket? = null) :
        Route<MessageCreatePacket>(
            Patch, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer(),
            RequestPayload(body = generateJsonBody(MessageEditPacket.serializer(), MessageEditPacket(content, embed))),
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

    class CreateGuildEmoji(guildID: Long, name: String, image: String, roles: List<Long> = emptyList()) :
        Route<GuildEmojiPacket>(
            Post, "/guilds/$guildID/emojis", GuildEmojiPacket.serializer(),
            RequestPayload(
                body = generateJsonBody(
                    CreateGuildEmojiPacket.serializer(), CreateGuildEmojiPacket(name, image, roles)
                )
            )
        )

    class ModifyGuildEmoji(guildID: Long, emojiID: Long, name: String, roles: List<Long> = emptyList()) :
        Route<GuildEmojiPacket>(
            Patch, "/guilds/$guildID/emojis/$emojiID", GuildEmojiPacket.serializer(),
            RequestPayload(
                body = generateJsonBody(
                    ModifyGuildEmojiPacket.serializer(), ModifyGuildEmojiPacket(name, roles)
                )
            ),
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

    class ModifyGuild(
        guildID: Long,
        name: String? = null,
        region: String? = null,
        verification_level: Int? = null,
        default_message_notifications: Int? = null,
        explicit_content_filter: Int? = null,
        afk_channel_id: Long? = null,
        afk_timeout: Int? = null,
        icon: String? = null,
        owner_id: Long? = null,
        splash: String? = null,
        system_channel_id: Long? = null
    ) : Route<GuildCreatePacket>(
        Patch, "/guilds/$guildID", GuildCreatePacket.serializer(),
        RequestPayload(
            body = generateJsonBody(
                ModifyGuildPacket.serializer(),
                ModifyGuildPacket(
                    name,
                    region,
                    verification_level,
                    default_message_notifications,
                    explicit_content_filter,
                    afk_channel_id,
                    afk_timeout,
                    icon,
                    owner_id,
                    splash,
                    system_channel_id
                )
            )
        )
    )

    class DeleteGuild(guildID: Long) : Route<Nothing>(Delete, "/guild/$guildID")

    class GetGuildChannels(guildID: Long) : Route<List<GuildChannelPacket>>(
        Get, "/guilds/$guildID/channels", GuildChannelPacket.polymorphicSerializer.list
    )

    class CreateGuildChannel(
        guildID: Long,
        name: String,
        type: Int? = null,
        topic: String? = null,
        bitrate: Int? = null,
        user_limit: Int? = null,
        rate_limit_per_user: Int? = null,
        position: Int? = null,
        permission_overwrites: List<PermissionOverwritePacket>? = null,
        parent_id: Long? = null,
        nsfw: Boolean? = null
    ) : Route<GuildChannelPacket>(
        Post, "/guilds/$guildID/channels", GuildChannelPacket.polymorphicSerializer,
        RequestPayload(
            body = generateJsonBody(
                CreateGuildChannelPacket.serializer(),
                CreateGuildChannelPacket(
                    name,
                    type,
                    topic,
                    bitrate,
                    user_limit,
                    rate_limit_per_user,
                    position,
                    permission_overwrites,
                    parent_id,
                    nsfw
                )
            )
        )
    )

    class ModifyGuildChannelPositions(guildID: Long, positions: Map<Long, Int>) : Route<Nothing>(
        Patch, "/guilds/$guildID/channels", requestPayload =
        RequestPayload(body = generateJsonBody(
            ModifyPositionPacket.serializer().list, positions.map {
                ModifyPositionPacket(it.key, it.value)
            }
        ))
    )

    class GetGuildMember(guildID: Long, userID: Long) : Route<GuildMemberPacket>(
        Get, "/guilds/$guildID/members/$userID", GuildMemberPacket.serializer(),
        ratelimitPath = "/guilds/$guildID/members/userID"
    )

    /**
     * @param limit max number of members to return (1-1_000)
     * @param after The member at the end of the last pagination.
     */
    class ListGuildMembers(guildID: Long, limit: Int = 1, after: Long = 0) : Route<List<GuildMemberPacket>>(
        Get, "/guilds/$guildID/members", GuildMemberPacket.serializer().list,
        RequestPayload(mapOf("limit" to limit.toString(), "after" to after.toString()))
    )

    class ModifyGuildMember(
        guildID: Long,
        userID: Long,
        nick: String? = null,
        roles: List<Long>? = null,
        mute: Boolean? = null,
        deaf: Boolean? = null,
        channel_id: Long? = null
    ) : Route<Nothing>(
        Patch, "/guilds/$guildID/members/$userID", requestPayload =
        RequestPayload(
            body = generateJsonBody(
                ModifyGuildMemberPacket.serializer(), ModifyGuildMemberPacket(nick, roles, mute, deaf, channel_id)
            )
        )
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

    class CreateGuildRole(
        guildID: Long,
        name: String? = null,
        permissions: Int? = null,
        color: Int = 0,
        hoist: Boolean = false,
        mentionable: Boolean = false
    ) : Route<GuildRolePacket>(
        Post, "/guilds/$guildID/roles", serializer = GuildRolePacket.serializer(),
        requestPayload = RequestPayload(
            body = generateJsonBody(
                CreateGuildRolePacket.serializer(), CreateGuildRolePacket(name, permissions, color, hoist, mentionable)
            )
        )
    )

    class ModifyGuildRole(
        guildID: Long,
        roleID: Long,
        name: String? = null,
        permissions: Int? = null,
        color: Int = 0,
        hoist: Boolean = false,
        mentionable: Boolean = false
    ) : Route<GuildRolePacket>(
        Patch, "/guilds/$guildID/roles/$roleID", serializer = GuildRolePacket.serializer(),
        requestPayload = RequestPayload(
            body = generateJsonBody(
                CreateGuildRolePacket.serializer(), CreateGuildRolePacket(name, permissions, color, hoist, mentionable)
            )
        )
    )

    class DeleteGuildRole(guildID: Long, roleID: Long) : Route<Nothing>(
        Delete, "/guilds/$guildID/roles/$roleID", ratelimitPath = "/guilds/$guildID/roles/roleID"
    )

    class ModifyGuildRolePosition(guildID: Long, positions: Map<Long, Int>) :
        Route<List<GuildRolePacket>>(
            Patch, "/guilds/$guildID/roles", serializer = GuildRolePacket.serializer().list,
            requestPayload = RequestPayload(body = generateJsonBody(
                ModifyPositionPacket.serializer().list,
                positions.map { ModifyPositionPacket(it.key, it.value) }
            ))
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

    class CreateGuildIntegration(guildID: Long, type: String, id: Long) : Route<Nothing>(
        Post, "/guilds/$guildID/integrations",
        requestPayload = RequestPayload(
            body = generateJsonBody(
                CreateGuildIntegrationPacket.serializer(), CreateGuildIntegrationPacket(type, id)
            )
        )
    )

    class ModifyGuildIntegration(
        guildID: Long,
        integrationID: Long,
        expire_behavior: Int,
        expire_grace_period: Int,
        enable_emoticons: Boolean
    ) : Route<Nothing>(
        Patch, "/guilds/$guildID/integrations/$integrationID",
        requestPayload = RequestPayload(
            body = generateJsonBody(
                ModifyGuildIntegrationPacket.serializer(),
                ModifyGuildIntegrationPacket(expire_behavior, expire_grace_period, enable_emoticons)
            )
        )
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

    /** All params are optional filters other than [guildID]. */
    class GetGuildAuditLog(
        guildID: Long,
        userID: Long? = null,
        eventType: AuditLogEvent? = null,
        before: Long? = null,
        limit: Int? = null
    ) : Route<AuditLogPacket>(
        Get, "/guilds/$guildID/audit-logs", AuditLogPacket.serializer(), RequestPayload(
            mutableMapOf<String, String>().apply {
                userID?.let { put("user_id", "$it") }
                eventType?.let { put("action_type", "$it") }
                before?.let { put("before", "$it") }
                limit?.let { put("limit", "$it") }
            }
        )
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

    // Webhook Routes

    class CreateWebhook(channelID: Long, name: String, avatarData: AvatarData? = null) : Route<WebhookPacket>(
        Post, "/channels/$channelID/webhooks", WebhookPacket.serializer(), RequestPayload(
            body = generateJsonBody(CreateWebhookPacket.serializer(), CreateWebhookPacket(name, avatarData?.dataUri))
        )
    )

    class GetChannelWebhooks(channelID: Long) : Route<List<WebhookPacket>>(
        Get, "/channels/$channelID/webhooks", WebhookPacket.serializer().list
    )

    class GetGuildWebhooks(guildID: Long) : Route<List<WebhookPacket>>(
        Get, "/guilds/$guildID/webhooks", WebhookPacket.serializer().list
    )

    class GetWebhook(webhookID: Long) : Route<WebhookPacket>(
        Get, "/webhooks/$webhookID", WebhookPacket.serializer()
    )

    class GetWebhookWithToken(webhookID: Long, token: String) : Route<WebhookPacket>(
        Get, "/webhooks/$webhookID/$token", WebhookPacket.serializer(),
        ratelimitPath = "/webhooks/$webhookID/token"
    )

    class ModifyWebhook(
        webhookID: Long, name: String? = null, avatarData: AvatarData? = null, channelID: Long? = null
    ) : Route<WebhookPacket>(
        Patch, "/webhooks/$webhookID", WebhookPacket.serializer(), RequestPayload(
            body = generateJsonBody(
                ModifyWebhookPacket.serializer(), ModifyWebhookPacket(name, avatarData?.dataUri, channelID)
            )
        )
    )

    class ModifyWebhookWithToken(
        webhookID: Long, token: String, name: String? = null, avatarData: AvatarData? = null
    ) : Route<WebhookPacket>(
        Patch, "/webhooks/$webhookID/$token", WebhookPacket.serializer(), RequestPayload(
            body = generateJsonBody(
                ModifyWebhookPacket.serializer(), ModifyWebhookPacket(name, avatarData?.dataUri)
            )
        ), "/webhooks/$webhookID/token"
    )

    class DeleteWebhook(webhookID: Long) : Route<Unit>(Delete, "/webhooks/$webhookID")

    class DeleteWebhookWithToken(webhookID: Long, token: String) : Route<Unit>(
        Delete, "/webhooks/$webhookID/$token", ratelimitPath = "/webhooks/$webhookID/token"
    )

    class ExecuteWebhook(
        webhookID: Long,
        token: String,
        content: String? = null,
        username: String? = null,
        avatar_url: String? = null,
        tts: Boolean? = null,
        file: String? = null,
        embeds: List<OutgoingEmbedPacket>? = null,
        payload_json: String? = null
    ) : Route<Unit>(
        Post, "/webhooks/$webhookID/$token", requestPayload = RequestPayload(
            mapOf("wait" to "false"), generateJsonBody(
                ExecuteWebhookPacket.serializer(),
                ExecuteWebhookPacket(content, username, avatar_url, tts, file, embeds, payload_json)
            )
        ), ratelimitPath = "/webhooks/$webhookID/token"
    )

    class ExecuteWebhookAndWait(
        webhookID: Long,
        token: String,
        content: String? = null,
        username: String? = null,
        avatar_url: String? = null,
        tts: Boolean? = null,
        file: String? = null,
        embeds: List<OutgoingEmbedPacket>? = null,
        payload_json: String? = null
    ) : Route<MessageCreatePacket>(
        Post, "/webhooks/$webhookID/$token", MessageCreatePacket.serializer(), RequestPayload(
            mapOf("wait" to "true"), generateJsonBody(
                ExecuteWebhookPacket.serializer(),
                ExecuteWebhookPacket(content, username, avatar_url, tts, file, embeds, payload_json)
            )
        ), "/webhooks/$webhookID/token"
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
