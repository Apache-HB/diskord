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

private class RouteBuilder<R : Any>(val method: HttpMethod, val path: String, val serializer: KSerializer<R>?) {
    var ratelimitKey: String? = null
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

    fun build() = Route(method, "${baseUri}$path", ratelimitKey ?: path, serializer, parameters, body)

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
    init: RouteBuilder<Nothing>.() -> Unit = {}
) = RouteBuilder<Nothing>(method, path, null).apply(init).build()

private inline fun <reified R : Any> route(
    method: HttpMethod,
    path: String,
    serializer: KSerializer<R>? = null,
    init: RouteBuilder<R>.() -> Unit = {}
) = RouteBuilder(method, path, serializer ?: serializer()).apply(init).build()

internal class Route<R : Any>(
    val method: HttpMethod,
    val uri: String,
    val ratelimitKey: String,
    val serializer: KSerializer<R>?,
    val parameters: Map<String, String>,
    val body: OutgoingContent
) {
    @Suppress("FunctionName")
    companion object {
        fun GetChannel(id: Long) = route(Get, "/channels/$id", ChannelPacket.polymorphicSerializer)

        fun ModifyChannel(id: Long, packet: ModifyChannelPacket) =
            route(Patch, "/channels/$id", ChannelPacket.polymorphicSerializer) {
                body(packet)
            }

        fun DeleteChannel(id: Long) = route(Delete, "/channels/$id", ChannelPacket.polymorphicSerializer)

        fun GetChannelInvites(id: Long) = route<List<InviteMetadataPacket>>(Get, "/channels/$id/invites")

        fun CreateChannelInvite(
            channelID: Long,
            maxAge: Int? = null, maxUses: Int? = null, temporary: Boolean? = null, unique: Boolean? = null
        ) = route<InvitePacket>(Post, "/channels/$channelID/invites") {
            body(CreateChannelInvitePacket(maxAge, maxUses, temporary, unique))
        }

        fun EditChannelPermissions(id: Long, override: PermissionOverride) =
            route(Put, "/channels/$id/permissions/${override.id}") {
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
            route(Delete, "/channels/$channelID/permissions/$overrideID") {
                ratelimitKey = "/channels/$channelID/permissions/overrideID"
            }

        fun TriggerTypingIndicator(channelID: Long) = route(Post, "/channels/$channelID/typing") {
            // no idea why this is necessary, but discord seems to require it so ¯\_(ツ)_/¯
            body("")
        }

        fun GetPinnedMessages(channelID: Long) = route<List<MessageCreatePacket>>(Get, "/channels/$channelID/pins")

        fun AddPinnedChannelMessage(channelID: Long, messageID: Long) =
            route(Put, "/channels/$channelID/pins/$messageID") {
                ratelimitKey = "/channels/$channelID/pins/messageID"
            }

        fun DeletePinnedChannelMessage(channelID: Long, messageID: Long) =
            route(Delete, "/channels/$channelID/pins/$messageID") {
                ratelimitKey = "/channels/$channelID/pins/messageID"
            }

        fun GetChannelMessages(
            id: Long,
            around: Long? = null, before: Long? = null, after: Long? = null,
            limit: Int? = null
        ) = route<List<MessageCreatePacket>>(Get, "/channels/$id/messages") {
            parameters("around" to around, "before" to before, "after" to after, "limit" to limit)
        }

        fun GetChannelMessage(channelID: Long, messageID: Long) =
            route<MessageCreatePacket>(Get, "/channels/$channelID/messages/$messageID") {
                ratelimitKey = "/channels/$channelID/messages/messageID"
            }

        fun CreateMessage(
            channelID: Long,
            text: String? = null, embed: OutgoingEmbedPacket? = null, tts: Boolean? = null
        ) = route<MessageCreatePacket>(Post, "/channels/$channelID/messages") {
            body(MessageSendPacket(text, tts, embed))
        }

        fun CreateMessage(channelID: Long, fileName: String, fileData: ByteArray) =
            route<MessageCreatePacket>(Post, "/channels/$channelID/messages") {
                body {
                    val headers = headersOf(
                        HttpHeaders.ContentDisposition,
                        """form-data; name="file"; filename="$fileName""""
                    )
                    append("file", fileData, headers)
                }
            }

        suspend fun CreateReaction(channelID: Long, messageID: Long, emoji: Emoji) =
            route(Put, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}/@me") {
                body(emoji.getRequestData())
                ratelimitKey = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
            }

        suspend fun DeleteOwnReaction(channelID: Long, messageID: Long, emoji: Emoji) =
            route(Delete, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}/@me") {
                body(emoji.getRequestData())
                ratelimitKey = "/channels/$channelID/messages/messageID/reactions/emoji/@me"
            }

        suspend fun DeleteUserReaction(channelID: Long, messageID: Long, userID: Long, emoji: Emoji) =
            route(Delete, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}/$userID") {
                body(emoji.getRequestData())
                ratelimitKey = "/channels/$channelID/messages/messageID/reactions/emoji/userID"
            }

        suspend fun GetReactions(
            channelID: Long, messageID: Long, emoji: Emoji,
            before: Long? = null, after: Long? = null, limit: Int? = null
        ) = route<List<UserPacket>>(Get, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}") {
            parameters("before" to before, "after" to after, "limit" to limit)
            ratelimitKey = "/channels/$channelID/messages/messageID/reactions/emoji"
        }

        fun DeleteAllReactions(channelID: Long, messageID: Long) =
            route(Delete, "/channels/$channelID/messages/$messageID/reactions") {
                ratelimitKey = "/channels/$channelID/messages/messageID/reactions"
            }

        fun EditMessage(channelID: Long, id: Long, text: String? = null, embed: OutgoingEmbedPacket? = null) =
            route<MessageCreatePacket>(Patch, "/channels/$channelID/messages/$id") {
                body(MessageEditPacket(text, embed))
                ratelimitKey = "/channels/$channelID/messages/messageID"
            }

        fun DeleteMessage(channelID: Long, id: Long) = route(Delete, "/channels/$channelID/messages/$id") {
            // this is formatted differently due to Discord's policy for rate limiting message deletion by bots
            ratelimitKey = "/channels/$channelID/messages/messageID?delete"
        }

        fun BulkDeleteMessages(channelID: Long, messageIDs: List<Long>) =
            route(Post, "/channels/$channelID/messages/bulk-delete") {
                body(BulkDeleteMessagesPacket(messageIDs))
            }

        fun ListGuildEmojis(guildID: Long) = route<List<GuildEmojiPacket>>(Get, "/guilds/$guildID/emojis")

        fun GetGuildEmoji(guildID: Long, id: Long) = route<GuildEmojiPacket>(Get, "/guilds/$guildID/emojis/$id") {
            ratelimitKey = "/guilds/$guildID/emojis/emojiID"
        }

        fun CreateGuildEmoji(guildID: Long, name: String, imageData: ByteArray, roles: List<Long> = emptyList()) =
            route<GuildEmojiPacket>(Post, "/guilds/$guildID/emojis") {
                body("name" to name, "image" to encodeBase64(imageData), "roles" to roles)
            }

        fun ModifyGuildEmoji(guildID: Long, id: Long, newName: String, newRoles: List<Long> = emptyList()) =
            route<GuildEmojiPacket>(Patch, "/guilds/$guildID/emojis/$id") {
                body("name" to newName, "roles" to newRoles)
                ratelimitKey = "/guilds/$guildID/emojis/emojiID"
            }

        fun DeleteGuildEmoji(guildID: Long, id: Long) = route(Delete, "/guilds/$guildID/emojis/$id") {
            ratelimitKey = "/guilds/$guildID/emojis/emojiID"
        }

        fun CreateGuild(packet: CreateGuildPacket) = route(Post, "/guilds") {
            body(packet)
        }

        fun GetGuild(id: Long) = route<GuildCreatePacket>(Get, "/guilds/$id")

        fun ModifyGuild(id: Long, packet: ModifyGuildPacket) = route<GuildCreatePacket>(Patch, "/guilds/$id") {
            body(packet)
        }

        fun DeleteGuild(id: Long) = route(Delete, "/guild/$id")

        fun GetGuildChannels(id: Long) =
            route(Get, "/guilds/$id/channels", ListSerializer(GuildChannelPacket.polymorphicSerializer))

        fun CreateGuildChannel(guildID: Long, packet: CreateGuildChannelPacket) =
            route(Post, "/guilds/$guildID/channels", GuildChannelPacket.polymorphicSerializer) {
                body(packet)
            }

        fun ModifyGuildChannelPositions(guildID: Long, positions: Map<Long, Int>) =
            route(Patch, "/guilds/$guildID/channels") {
                body(positions.map { ModifyPositionPacket(it.key, it.value) })
            }

        fun GetGuildMember(guildID: Long, userID: Long) =
            route<GuildMemberPacket>(Get, "/guilds/$guildID/members/$userID") {
                ratelimitKey = "/guilds/$guildID/members/userID"
            }

        fun ListGuildMembers(guildID: Long, limit: Int? = null, after: Long? = null) =
            route<List<GuildMemberPacket>>(Get, "/guilds/$guildID/members") {
                parameters("limit" to limit, "after" to after)
            }

        fun ModifyGuildMember(guildID: Long, userID: Long, packet: ModifyGuildMemberPacket) =
            route(Patch, "/guilds/$guildID/members/$userID") {
                body(packet)
            }

        fun ModifyCurrentUserNick(guildID: Long, nickname: String) = route(Patch, "/guilds/$guildID/members/@me/nick") {
            body("nick" to nickname)
        }

        fun RemoveGuildMember(guildID: Long, userID: Long) = route(Delete, "/guilds/$guildID/members/$userID") {
            ratelimitKey = "/guilds/$guildID/members/$userID"
        }

        fun GetGuildBans(guildID: Long) = route<List<BanPacket>>(Get, "/guilds/$guildID/bans")

        fun GetGuildBan(guildID: Long, userID: Long) = route<BanPacket>(Get, "/guilds/$guildID/bans/$userID") {
            ratelimitKey = "/guilds/$guildID/bans/userID"
        }

        fun CreateGuildBan(guildID: Long, userID: Long, deleteMessageDays: Int = 0, reason: String? = null) =
            route(Put, "/guilds/$guildID/bans/$userID") {
                parameters("delete-message-days" to deleteMessageDays, "reason" to reason)
                ratelimitKey = "/guilds/$guildID/members/userID"
            }

        fun RemoveGuildBan(guildID: Long, userID: Long) = route(Delete, "/guilds/$guildID/bans/$userID") {
            ratelimitKey = "/guilds/$guildID/bans/userID"
        }

        fun GetGuildRoles(guildID: Long) = route<List<GuildRolePacket>>(Get, "/guilds/$guildID/roles")

        fun CreateGuildRole(guildID: Long, packet: CreateGuildRolePacket) =
            route<GuildRolePacket>(Post, "/guilds/$guildID/roles") {
                body(packet)
            }

        fun ModifyGuildRole(guildID: Long, roleID: Long, packet: CreateGuildRolePacket) =
            route<GuildRolePacket>(Patch, "/guilds/$guildID/roles/$roleID") {
                body(packet)
            }

        fun DeleteGuildRole(guildID: Long, roleID: Long) = route(Delete, "/guilds/$guildID/roles/$roleID") {
            ratelimitKey = "/guilds/$guildID/roles/roleID"
        }

        fun ModifyGuildRolePosition(guildID: Long, positions: Map<Long, Int>) =
            route<List<GuildRolePacket>>(Patch, "/guilds/$guildID/roles") {
                body(positions.map { ModifyPositionPacket(it.key, it.value) })
            }

        fun AddGuildMemberRole(guildID: Long, userID: Long, roleID: Long) =
            route(Put, "/guilds/$guildID/members/$userID/roles/$roleID")

        fun RemoveGuildMemberRole(guildID: Long, userID: Long, roleID: Long) =
            route(Delete, "/guilds/$guildID/members/$userID/roles/$roleID")

        fun GetGuildPruneCount(guildID: Long, days: Int? = null) =
            route<PruneCountPacket>(Get, "/guilds/$guildID/prune") {
                parameters("days" to days)
            }

        fun BeginGuildPrune(guildID: Long, days: Int? = null, computePruneCount: Boolean = true) =
            route<PruneCountPacket>(Post, "/guilds/$guildID/prune") {
                parameters("days" to days, "compute_prune_count" to computePruneCount)
            }

        fun GetGuildIntegrations(guildID: Long) =
            route<List<GuildIntegrationPacket>>(Get, "/guilds/$guildID/integrations")

        fun CreateGuildIntegration(guildID: Long, type: String, id: Long) =
            route(Post, "/guilds/$guildID/integrations") {
                body("type" to type, "id" to id)
            }

        fun ModifyGuildIntegration(guildID: Long, integrationID: Long, packet: ModifyGuildIntegrationPacket) =
            route(Patch, "/guilds/$guildID/integrations/$integrationID") {
                body(packet)
            }

        fun DeleteGuildIntegration(guildID: Long, integrationID: Long) =
            route(Delete, "/guilds/$guildID/integrations/$integrationID")

        fun SyncGuildIntegration(guildID: Long, integrationID: Long) =
            route(Post, "/guilds/$guildID/integrations/$integrationID/sync")

        fun GetGuildInvites(guildID: Long) = route<List<InviteMetadataPacket>>(Get, "/guilds/$guildID/invites")

        fun GetGuildEmbed(guildID: Long) = route<GuildEmbedPacket>(Get, "/guilds/$guildID/embed")

        fun ModifyGuildEmbed(guildID: Long, enable: Boolean? = null, channelID: Long? = null) =
            route<GuildEmbedPacket>(Patch, "/guilds/$guildID/embed") {
                body("enabled" to enable, "channel_id" to channelID)
            }

        fun GetGuildVanityUrl(guildID: Long) = route<PartialInvitePacket>(Get, "/guilds/$guildID/vanity-url")

        fun GetGuildVoiceRegions(guildID: Long) = route<List<VoiceRegionPacket>>(Get, "/guilds/$guildID/regions")

        fun GetGuildAuditLog(
            guildID: Long,
            userID: Long? = null, eventType: AuditLogEvent? = null, before: Long? = null, limit: Int? = null
        ) = route<AuditLogPacket>(Get, "/guilds/$guildID/audit-logs") {
            parameters("user_id" to userID, "action_type" to eventType, "before" to before, "limit" to limit)
        }

        fun GetInvite(inviteCode: String, withCounts: Boolean) = route<InvitePacket>(Get, "/invites/$inviteCode") {
            parameters("with_counts" to withCounts)
            ratelimitKey = "/invites/inviteCode"
        }

        fun DeleteInvite(inviteCode: String) = route<InvitePacket>(Delete, "/invites/$inviteCode") {
            ratelimitKey = "/invites/inviteCode"
        }

        val GetCurrentUser = route<UserPacket>(Get, "/users/@me")

        fun GetUser(id: Long) = route<UserPacket>(Get, "/users/$id") {
            ratelimitKey = "/users/userID"
        }

        fun ModifyCurrentUser(username: String? = null, avatarData: AvatarData? = null) =
            route<UserPacket>(Patch, "/users/@me") {
                body(ModifyCurrentUserPacket(username, avatarData?.dataUri))
            }

        fun GetCurrentUserGuilds(before: Long? = null, after: Long? = null, limit: Int? = null) =
            route<List<PartialGuildPacket>>(Get, "/users/@me/guilds") {
                parameters("before" to before, "after" to after, "limit" to limit)
            }

        fun LeaveGuild(guildID: Long) = route(Delete, "/users/@me/guilds/$guildID")

        fun CreateDM(recipientID: Long) = route<DmChannelPacket>(Post, "/users/@me/channels") {
            body("recipient_id" to recipientID)
        }

        fun CreateWebhook(channelID: Long, name: String, avatarData: AvatarData? = null) =
            route<WebhookPacket>(Post, "/channels/$channelID/webhooks") {
                body(CreateWebhookPacket(name, avatarData?.dataUri))
            }

        fun GetChannelWebhooks(channelID: Long) = route<List<WebhookPacket>>(Get, "/channels/$channelID/webhooks")

        fun GetGuildWebhooks(guildID: Long) = route<List<WebhookPacket>>(Get, "/guilds/$guildID/webhooks")

        fun GetWebhook(webhookID: Long) = route<WebhookPacket>(Get, "/webhooks/$webhookID")

        fun GetWebhookWithToken(webhookID: Long, token: String) =
            route<WebhookPacket>(Get, "/webhooks/$webhookID/$token") {
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        fun ModifyWebhook(webhookID: Long, packet: ModifyWebhookPacket) =
            route<WebhookPacket>(Patch, "/webhooks/$webhookID") {
                body(packet)
            }

        fun ModifyWebhookWithToken(webhookID: Long, token: String, packet: ModifyWebhookWithTokenPacket) =
            route<WebhookPacket>(Patch, "/webhooks/$webhookID/$token") {
                body(packet)
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        fun DeleteWebhook(webhookID: Long) = route(Delete, "/webhooks/$webhookID")

        fun deleteWebhookWithToken(webhookID: Long, token: String) = route(Delete, "/webhooks/$webhookID/$token") {
            ratelimitKey = "/webhooks/$webhookID/token"
        }

        fun ExecuteWebhook(webhookID: Long, token: String, packet: ExecuteWebhookPacket) =
            route(Post, "/webhooks/$webhookID/$token") {
                parameters("wait" to false)
                body(packet)
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        fun ExecuteWebhookAndWait(webhookID: Long, token: String, packet: ExecuteWebhookPacket) =
            route<MessageCreatePacket>(Post, "/webhooks/$webhookID/$token") {
                parameters("wait" to true)
                body(packet)
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        val GetGatewayBot = route(Get, "/gateway/bot")

        val ListVoiceRegions = route<List<VoiceRegionPacket>>(Get, "/voice/regions")

        val GetApplicationInfo = route<ApplicationInfoPacket>(Get, "/oauth2/applications/@me")
    }
}
