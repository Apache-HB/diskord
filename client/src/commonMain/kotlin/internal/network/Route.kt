package com.serebit.strife.internal.network

import com.serebit.strife.data.*
import com.serebit.strife.entities.Emoji
import com.serebit.strife.internal.encodeBase64
import com.serebit.strife.internal.packets.*
import io.ktor.client.request.forms.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.content.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

private class RouteBuilder<R : Any>(
    val method: HttpMethod,
    val path: String,
    val ratelimitKey: String,
    val majorParameter: Long? = null,
    val serializer: KSerializer<R>?,
) {
    var parameters: Map<String, String> = emptyMap()
    private var body: OutgoingContent = EmptyContent

    fun parameters(vararg pairs: Pair<String, Any?>) = pairs
        .filter { it.second != null }
        .toMap()
        .mapValues { it.value!!.toString() }

    fun body(text: String) {
        body = TextContent(text, ContentType.Text.Plain)
    }

    fun body(vararg pairs: Pair<String, Any?>) {
        val map = pairs.toMap().mapValues { it.value?.toString() }
        val text = json.encodeToString(map)
        body = TextContent(text, ContentType.Application.Json)
    }

    inline fun <reified T : Any> body(data: T) {
        body = TextContent(json.encodeToString(serializer(), data), ContentType.Application.Json)
    }

    fun body(builder: FormBuilder.() -> Unit) {
        body = MultiPartFormDataContent(formData(builder))
    }

    fun build() = Route(method, "${baseUri}$path", ratelimitKey, majorParameter, serializer, parameters, body)

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discord.com/api/v$apiVersion"

        private val json = Json {
            encodeDefaults = false
        }
    }
}

private inline fun route(
    method: HttpMethod,
    path: String,
    ratelimitKey: String,
    majorParameter: Long? = null,
    init: RouteBuilder<Nothing>.() -> Unit = {}
) = RouteBuilder<Nothing>(method, path, ratelimitKey, majorParameter, null).apply(init).build()

private inline fun <reified R : Any> route(
    method: HttpMethod,
    path: String,
    ratelimitKey: String,
    majorParameter: Long? = null,
    serializer: KSerializer<R>? = null,
    init: RouteBuilder<R>.() -> Unit = {}
) = RouteBuilder(method, path, ratelimitKey, majorParameter, serializer ?: serializer()).apply(init).build()

internal class Route<R : Any>(
    val method: HttpMethod,
    val uri: String,
    val ratelimitKey: String,
    val majorParameter: Long? = null,
    val serializer: KSerializer<R>?,
    val parameters: Map<String, String>,
    val body: OutgoingContent,
) {
    @Suppress("FunctionName")
    companion object {
        fun GetChannel(id: Long) = route(Get, "/channels/$id", "GetChannel", id, ChannelPacket.polymorphicSerializer)

        fun ModifyChannel(id: Long, packet: ModifyChannelPacket) =
            route(Patch, "/channels/$id", "ModifyChannel", id, ChannelPacket.polymorphicSerializer) {
                body(packet)
            }

        fun DeleteChannel(id: Long) = route(Delete, "/channels/$id", "DeleteChannel", id, ChannelPacket.polymorphicSerializer)

        fun GetChannelInvites(id: Long) = route<List<InviteMetadataPacket>>(Get, "/channels/$id/invites", "GetChannelInvites", id)

        fun CreateChannelInvite(
            channelID: Long,
            maxAge: Int? = null, maxUses: Int? = null, temporary: Boolean? = null, unique: Boolean? = null
        ) = route<InvitePacket>(Post, "/channels/$channelID/invites", "CreateChannelInvite", channelID) {
            body(CreateChannelInvitePacket(maxAge, maxUses, temporary, unique))
        }

        fun EditChannelPermissions(id: Long, override: PermissionOverride) =
            route(Put, "/channels/$id/permissions/${override.id}", "EditChannelPermissions", id) {
                parameters(
                    "allow" to override.allow.toBitSet(),
                    "deny" to override.deny.toBitSet(),
                    "type" to when (override) {
                        is RolePermissionOverride -> "role"
                        is MemberPermissionOverride -> "member"
                    }
                )
            }

        fun DeleteChannelPermission(channelID: Long, overrideID: Long) =
            route(Delete, "/channels/$channelID/permissions/$overrideID", "DeleteChannelPermission", channelID)

        fun TriggerTypingIndicator(channelID: Long) = route(Post, "/channels/$channelID/typing", "TriggerTypingIndicator", channelID) {
            // no idea why this is necessary, but discord seems to require it so ¯\_(ツ)_/¯
            body("")
        }

        fun GetPinnedMessages(channelID: Long) = route<List<MessageCreatePacket>>(Get, "/channels/$channelID/pins", "GetPinnedMessages", channelID)

        fun AddPinnedChannelMessage(channelID: Long, messageID: Long) =
            route(Put, "/channels/$channelID/pins/$messageID", "AddPinnedChannelMessage", channelID)

        fun DeletePinnedChannelMessage(channelID: Long, messageID: Long) =
            route(Delete, "/channels/$channelID/pins/$messageID", "DeletePinnedChannelMessage", channelID)

        fun GetChannelMessages(
            id: Long,
            around: Long? = null, before: Long? = null, after: Long? = null,
            limit: Int? = null
        ) = route<List<MessageCreatePacket>>(Get, "/channels/$id/messages", "GetChannelMessages", id) {
            parameters("around" to around, "before" to before, "after" to after, "limit" to limit)
        }

        fun GetChannelMessage(channelID: Long, messageID: Long) =
            route<MessageCreatePacket>(Get, "/channels/$channelID/messages/$messageID", "GetChannelMessage", channelID)

        fun CreateMessage(
            channelID: Long,
            text: String? = null, embed: OutgoingEmbedPacket? = null, tts: Boolean? = null
        ) = route<MessageCreatePacket>(Post, "/channels/$channelID/messages", "CreateMessage", channelID) {
            body(MessageSendPacket(text, tts, embed))
        }

        fun CreateMessage(channelID: Long, fileName: String, fileData: ByteArray) =
            route<MessageCreatePacket>(Post, "/channels/$channelID/messages", "CreateMessage", channelID) {
                body {
                    val headers = headersOf(
                        HttpHeaders.ContentDisposition,
                        """form-data; name="file"; filename="$fileName""""
                    )
                    append("file", fileData, headers)
                }
            }

        suspend fun CreateReaction(channelID: Long, messageID: Long, emoji: Emoji) =
            route(Put, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}/@me", "CreateReaction", channelID) {
                body(emoji.getRequestData())
            }

        suspend fun DeleteOwnReaction(channelID: Long, messageID: Long, emoji: Emoji) =
            route(Delete, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}/@me", "DeleteOwnReaction", channelID) {
                body(emoji.getRequestData())
            }

        suspend fun DeleteUserReaction(channelID: Long, messageID: Long, userID: Long, emoji: Emoji) =
            route(Delete, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}/$userID", "DeleteUserReaction", channelID) {
                body(emoji.getRequestData())
            }

        suspend fun GetReactions(
            channelID: Long, messageID: Long, emoji: Emoji,
            before: Long? = null, after: Long? = null, limit: Int? = null
        ) = route<List<UserPacket>>(Get, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}", "GetReactions", channelID) {
            parameters("before" to before, "after" to after, "limit" to limit)
        }

        fun DeleteAllReactions(channelID: Long, messageID: Long) =
            route(Delete, "/channels/$channelID/messages/$messageID/reactions", "DeleteAllReactions", channelID)

        fun EditMessage(channelID: Long, id: Long, text: String? = null, embed: OutgoingEmbedPacket? = null) =
            route<MessageCreatePacket>(Patch, "/channels/$channelID/messages/$id", "EditMessage", channelID) {
                body(MessageEditPacket(text, embed))
            }

        fun DeleteMessage(channelID: Long, id: Long) = route(Delete, "/channels/$channelID/messages/$id", "DeleteMessage", channelID)

        fun BulkDeleteMessages(channelID: Long, messageIDs: List<Long>) =
            route(Post, "/channels/$channelID/messages/bulk-delete", "BulkDeleteMessages", channelID) {
                body(BulkDeleteMessagesPacket(messageIDs))
            }

        fun ListGuildEmojis(guildID: Long) = route<List<GuildEmojiPacket>>(Get, "/guilds/$guildID/emojis", "ListGuildEmojis", guildID)

        fun GetGuildEmoji(guildID: Long, id: Long) = route<GuildEmojiPacket>(Get, "/guilds/$guildID/emojis/$id", "GetGuildEmoji", guildID)

        fun CreateGuildEmoji(guildID: Long, name: String, imageData: ByteArray, roles: List<Long> = emptyList()) =
            route<GuildEmojiPacket>(Post, "/guilds/$guildID/emojis", "CreateGuildEmoji", guildID) {
                body("name" to name, "image" to encodeBase64(imageData), "roles" to roles)
            }

        fun ModifyGuildEmoji(guildID: Long, id: Long, newName: String, newRoles: List<Long> = emptyList()) =
            route<GuildEmojiPacket>(Patch, "/guilds/$guildID/emojis/$id", "ModifyGuildEmoji", guildID) {
                body("name" to newName, "roles" to newRoles)
            }

        fun DeleteGuildEmoji(guildID: Long, id: Long) = route(Delete, "/guilds/$guildID/emojis/$id", "DeleteGuildEmoji", guildID)

        fun CreateGuild(packet: CreateGuildPacket) = route(Post, "/guilds", "CreateGuild") {
            body(packet)
        }

        fun GetGuild(id: Long) = route<GuildCreatePacket>(Get, "/guilds/$id", "GetGuild", id)

        fun ModifyGuild(id: Long, packet: ModifyGuildPacket) = route<GuildCreatePacket>(Patch, "/guilds/$id", "ModifyGuild", id) {
            body(packet)
        }

        fun DeleteGuild(id: Long) = route(Delete, "/guild/$id", "DeleteGuild", id)

        fun GetGuildChannels(id: Long) =
            route(Get, "/guilds/$id/channels", "GetGuildChannels", id,  ListSerializer(GuildChannelPacket.polymorphicSerializer))

        fun CreateGuildChannel(guildID: Long, packet: CreateGuildChannelPacket) =
            route(Post, "/guilds/$guildID/channels", "CreateGuildChannel", guildID, GuildChannelPacket.polymorphicSerializer) {
                body(packet)
            }

        fun ModifyGuildChannelPositions(guildID: Long, positions: Map<Long, Int>) =
            route(Patch, "/guilds/$guildID/channels", "ModifyGuildChannelPositions", guildID) {
                body(positions.map { ModifyPositionPacket(it.key, it.value) })
            }

        fun GetGuildMember(guildID: Long, userID: Long) =
            route<GuildMemberPacket>(Get, "/guilds/$guildID/members/$userID", "GetGuildMember", guildID)

        fun ListGuildMembers(guildID: Long, limit: Int? = null, after: Long? = null) =
            route<List<GuildMemberPacket>>(Get, "/guilds/$guildID/members", "ListGuildMembers", guildID) {
                parameters("limit" to limit, "after" to after)
            }

        fun ModifyGuildMember(guildID: Long, userID: Long, packet: ModifyGuildMemberPacket) =
            route(Patch, "/guilds/$guildID/members/$userID", "ModifyGuildMember", guildID) {
                body(packet)
            }

        fun ModifyCurrentUserNick(guildID: Long, nickname: String) = route(Patch, "/guilds/$guildID/members/@me/nick", "ModifyCurrentUserNick", guildID) {
            body("nick" to nickname)
        }

        fun RemoveGuildMember(guildID: Long, userID: Long) = route(Delete, "/guilds/$guildID/members/$userID", "RemoveGuildMember", guildID)

        fun GetGuildBans(guildID: Long) = route<List<BanPacket>>(Get, "/guilds/$guildID/bans", "GetGuildBans", guildID)

        fun GetGuildBan(guildID: Long, userID: Long) = route<BanPacket>(Get, "/guilds/$guildID/bans/$userID", "GetGuildBan", guildID)

        fun CreateGuildBan(guildID: Long, userID: Long, deleteMessageDays: Int = 0, reason: String? = null) =
            route(Put, "/guilds/$guildID/bans/$userID", "CreateGuildBan", guildID) {
                parameters("delete-message-days" to deleteMessageDays, "reason" to reason)
            }

        fun RemoveGuildBan(guildID: Long, userID: Long) = route(Delete, "/guilds/$guildID/bans/$userID", "RemoveGuildBan", guildID)

        fun GetGuildRoles(guildID: Long) = route<List<GuildRolePacket>>(Get, "/guilds/$guildID/roles", "GetGuildRoles", guildID)

        fun CreateGuildRole(guildID: Long, packet: CreateGuildRolePacket) =
            route<GuildRolePacket>(Post, "/guilds/$guildID/roles", "CreateGuildRole", guildID) {
                body(packet)
            }

        fun ModifyGuildRole(guildID: Long, roleID: Long, packet: CreateGuildRolePacket) =
            route<GuildRolePacket>(Patch, "/guilds/$guildID/roles/$roleID", "ModifyGuildRole", guildID) {
                body(packet)
            }

        fun DeleteGuildRole(guildID: Long, roleID: Long) = route(Delete, "/guilds/$guildID/roles/$roleID", "DeleteGuildRole", guildID)

        fun ModifyGuildRolePosition(guildID: Long, positions: Map<Long, Int>) =
            route<List<GuildRolePacket>>(Patch, "/guilds/$guildID/roles", "ModifyGuildRolePosition", guildID) {
                body(positions.map { ModifyPositionPacket(it.key, it.value) })
            }

        fun AddGuildMemberRole(guildID: Long, userID: Long, roleID: Long) =
            route(Put, "/guilds/$guildID/members/$userID/roles/$roleID", "AddGuildMemberRole", guildID)

        fun RemoveGuildMemberRole(guildID: Long, userID: Long, roleID: Long) =
            route(Delete, "/guilds/$guildID/members/$userID/roles/$roleID", "RemoveGuildMemberRole", guildID)

        fun GetGuildPruneCount(guildID: Long, days: Int? = null) =
            route<PruneCountPacket>(Get, "/guilds/$guildID/prune", "GetGuildPruneCount", guildID) {
                parameters("days" to days)
            }

        fun BeginGuildPrune(guildID: Long, days: Int? = null, computePruneCount: Boolean = true) =
            route<PruneCountPacket>(Post, "/guilds/$guildID/prune", "BeginGuildPrune", guildID) {
                parameters("days" to days, "compute_prune_count" to computePruneCount)
            }

        fun GetGuildIntegrations(guildID: Long) =
            route<List<GuildIntegrationPacket>>(Get, "/guilds/$guildID/integrations", "GetGuildIntegrations", guildID)

        fun CreateGuildIntegration(guildID: Long, type: String, id: Long) =
            route(Post, "/guilds/$guildID/integrations", "CreateGuildIntegration", guildID) {
                body("type" to type, "id" to id)
            }

        fun ModifyGuildIntegration(guildID: Long, integrationID: Long, packet: ModifyGuildIntegrationPacket) =
            route(Patch, "/guilds/$guildID/integrations/$integrationID", "ModifyGuildIntegration", guildID) {
                body(packet)
            }

        fun DeleteGuildIntegration(guildID: Long, integrationID: Long) =
            route(Delete, "/guilds/$guildID/integrations/$integrationID", "DeleteGuildIntegration", guildID)

        fun SyncGuildIntegration(guildID: Long, integrationID: Long) =
            route(Post, "/guilds/$guildID/integrations/$integrationID/sync", "SyncGuildIntegration", guildID)

        fun GetGuildInvites(guildID: Long) = route<List<InviteMetadataPacket>>(Get, "/guilds/$guildID/invites", "GetGuildInvites", guildID)

        fun GetGuildEmbed(guildID: Long) = route<GuildEmbedPacket>(Get, "/guilds/$guildID/embed", "GetGuildEmbed", guildID)

        fun ModifyGuildEmbed(guildID: Long, enable: Boolean? = null, channelID: Long? = null) =
            route<GuildEmbedPacket>(Patch, "/guilds/$guildID/embed", "ModifyGuildEmbed", guildID) {
                body("enabled" to enable, "channel_id" to channelID)
            }

        fun GetGuildVanityUrl(guildID: Long) = route<PartialInvitePacket>(Get, "/guilds/$guildID/vanity-url", "GetGuildVanityUrl", guildID)

        fun GetGuildVoiceRegions(guildID: Long) = route<List<VoiceRegionPacket>>(Get, "/guilds/$guildID/regions", "GetGuildVoiceRegions", guildID)

        fun GetGuildAuditLog(
            guildID: Long,
            userID: Long? = null, eventType: AuditLogEvent? = null, before: Long? = null, limit: Int? = null
        ) = route<AuditLogPacket>(Get, "/guilds/$guildID/audit-logs", "GetGuildAuditLog", guildID) {
            parameters("user_id" to userID, "action_type" to eventType, "before" to before, "limit" to limit)
        }

        fun GetInvite(inviteCode: String, withCounts: Boolean) = route<InvitePacket>(Get, "/invites/$inviteCode", "GetInvite") {
            parameters("with_counts" to withCounts)
        }

        fun DeleteInvite(inviteCode: String) = route<InvitePacket>(Delete, "/invites/$inviteCode", "DeleteInvite") {
        }

        val GetCurrentUser = route<UserPacket>(Get, "/users/@me", "GetCurrentUser")

        fun GetUser(id: Long) = route<UserPacket>(Get, "/users/$id", "GetUser")

        fun ModifyCurrentUser(username: String? = null, avatarData: AvatarData? = null) =
            route<UserPacket>(Patch, "/users/@me", "ModifyCurrentUser") {
                body(ModifyCurrentUserPacket(username, avatarData?.dataUri))
            }

        fun GetCurrentUserGuilds(before: Long? = null, after: Long? = null, limit: Int? = null) =
            route<List<PartialGuildPacket>>(Get, "/users/@me/guilds", "GetCurrentUserGuilds") {
                parameters("before" to before, "after" to after, "limit" to limit)
            }

        fun LeaveGuild(guildID: Long) = route(Delete, "/users/@me/guilds/$guildID", "LeaveGuild")

        fun CreateDM(recipientID: Long) = route<DmChannelPacket>(Post, "/users/@me/channels", "CreateDM") {
            body("recipient_id" to recipientID)
        }

        fun CreateWebhook(channelID: Long, name: String, avatarData: AvatarData? = null) =
            route<WebhookPacket>(Post, "/channels/$channelID/webhooks", "CreateWebhook", channelID) {
                body(CreateWebhookPacket(name, avatarData?.dataUri))
            }

        fun GetChannelWebhooks(channelID: Long) = route<List<WebhookPacket>>(Get, "/channels/$channelID/webhooks", "GetChannelWebhooks", channelID)

        fun GetGuildWebhooks(guildID: Long) = route<List<WebhookPacket>>(Get, "/guilds/$guildID/webhooks", "GetGuildWebhooks", guildID)

        fun GetWebhook(webhookID: Long) = route<WebhookPacket>(Get, "/webhooks/$webhookID", "GetWebhook", webhookID)

        fun GetWebhookWithToken(webhookID: Long, token: String) =
            route<WebhookPacket>(Get, "/webhooks/$webhookID/$token", "GetWebhookWithToken", webhookID)

        fun ModifyWebhook(webhookID: Long, packet: ModifyWebhookPacket) =
            route<WebhookPacket>(Patch, "/webhooks/$webhookID", "ModifyWebhook", webhookID) {
                body(packet)
            }

        fun ModifyWebhookWithToken(webhookID: Long, token: String, packet: ModifyWebhookWithTokenPacket) =
            route<WebhookPacket>(Patch, "/webhooks/$webhookID/$token", "ModifyWebhookWithToken", webhookID) {
                body(packet)
            }

        fun DeleteWebhook(webhookID: Long) = route(Delete, "/webhooks/$webhookID", "DeleteWebhook", webhookID)

        fun DeleteWebhookWithToken(webhookID: Long, token: String) = route(Delete, "/webhooks/$webhookID/$token", "DeleteWebhookWithToken", webhookID)

        fun ExecuteWebhook(webhookID: Long, token: String, packet: ExecuteWebhookPacket) =
            route(Post, "/webhooks/$webhookID/$token", "ExecuteWebhook", webhookID) {
                parameters("wait" to false)
                body(packet)
            }

        fun ExecuteWebhookAndWait(webhookID: Long, token: String, packet: ExecuteWebhookPacket) =
            route<MessageCreatePacket>(Post, "/webhooks/$webhookID/$token", "ExecuteWebhook", webhookID) {
                parameters("wait" to true)
                body(packet)
            }

        val GetGatewayBot = route(Get, "/gateway/bot", "GetGatewayBot")

        val ListVoiceRegions = route<List<VoiceRegionPacket>>(Get, "/voice/regions", "ListVoiceRegions")

        val GetApplicationInfo = route<ApplicationInfoPacket>(Get, "/oauth2/applications/@me", "GetApplicationInfo")
    }
}
