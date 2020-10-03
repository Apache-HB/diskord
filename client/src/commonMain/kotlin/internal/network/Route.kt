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
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

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
        val text = json.encodeToString(MapSerializer(String.serializer(), String.serializer().nullable), map)
        body = TextContent(text, ContentType.Application.Json)
    }

    fun <T : Any> body(serializer: KSerializer<T>, data: T) {
        body = TextContent(json.encodeToString(serializer, data), ContentType.Application.Json)
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
) = route(method, path, null, init)

private inline fun <R : Any> route(
    method: HttpMethod,
    path: String,
    serializer: KSerializer<R>? = null,
    init: RouteBuilder<R>.() -> Unit = {}
) = RouteBuilder(method, path, serializer).apply(init).build()

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
                body(ModifyChannelPacket.serializer(), packet)
            }

        fun DeleteChannel(id: Long) = route(Delete, "/channels/$id", ChannelPacket.polymorphicSerializer)

        fun GetChannelInvites(id: Long) = route(
            Get, "/channels/$id/invites",
            ListSerializer(InviteMetadataPacket.serializer())
        )

        fun CreateChannelInvite(
            channelID: Long,
            maxAge: Int? = null, maxUses: Int? = null, temporary: Boolean? = null, unique: Boolean? = null
        ) = route(Post, "/channels/$channelID/invites", InvitePacket.serializer()) {
            body(CreateChannelInvitePacket.serializer(), CreateChannelInvitePacket(maxAge, maxUses, temporary, unique))
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

        fun GetPinnedMessages(channelID: Long) = route(
            Get,
            "/channels/$channelID/pins",
            ListSerializer(MessageCreatePacket.serializer())
        )

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
        ) = route(Get, "/channels/$id/messages", ListSerializer(MessageCreatePacket.serializer())) {
            parameters("around" to around, "before" to before, "after" to after, "limit" to limit)
        }

        fun GetChannelMessage(channelID: Long, messageID: Long) =
            route(Get, "/channels/$channelID/messages/$messageID", MessageCreatePacket.serializer()) {
                ratelimitKey = "/channels/$channelID/messages/messageID"
            }

        fun CreateMessage(
            channelID: Long,
            text: String? = null, embed: OutgoingEmbedPacket? = null, tts: Boolean? = null
        ) = route(Post, "/channels/$channelID/messages", MessageCreatePacket.serializer()) {
            body(MessageSendPacket.serializer(), MessageSendPacket(text, tts, embed))
        }

        fun CreateMessage(channelID: Long, fileName: String, fileData: ByteArray) =
            route(Post, "/channels/$channelID/messages", MessageCreatePacket.serializer()) {
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
        ) = route(
            Get, "/channels/$channelID/messages/$messageID/reactions/${emoji.uriData()}",
            ListSerializer(UserPacket.serializer())
        ) {
            parameters("before" to before, "after" to after, "limit" to limit)
            ratelimitKey = "/channels/$channelID/messages/messageID/reactions/emoji"
        }

        fun DeleteAllReactions(channelID: Long, messageID: Long) =
            route(Delete, "/channels/$channelID/messages/$messageID/reactions") {
                ratelimitKey = "/channels/$channelID/messages/messageID/reactions"
            }

        fun EditMessage(channelID: Long, id: Long, text: String? = null, embed: OutgoingEmbedPacket? = null) =
            route(Patch, "/channels/$channelID/messages/$id", MessageCreatePacket.serializer()) {
                body(MessageEditPacket.serializer(), MessageEditPacket(text, embed))
                ratelimitKey = "/channels/$channelID/messages/messageID"
            }

        fun DeleteMessage(channelID: Long, id: Long) = route(Delete, "/channels/$channelID/messages/$id") {
            // this is formatted differently due to Discord's policy for rate limiting message deletion by bots
            ratelimitKey = "/channels/$channelID/messages/messageID?delete"
        }

        fun BulkDeleteMessages(channelID: Long, messageIDs: List<Long>) =
            route(Post, "/channels/$channelID/messages/bulk-delete") {
                body(BulkDeleteMessagesPacket.serializer(), BulkDeleteMessagesPacket(messageIDs))
            }

        fun ListGuildEmojis(guildID: Long) = route(
            Get, "/guilds/$guildID/emojis",
            ListSerializer(GuildEmojiPacket.serializer())
        )

        fun GetGuildEmoji(guildID: Long, id: Long) =
            route(Get, "/guilds/$guildID/emojis/$id", GuildEmojiPacket.serializer()) {
                ratelimitKey = "/guilds/$guildID/emojis/emojiID"
            }

        fun CreateGuildEmoji(guildID: Long, name: String, imageData: ByteArray, roles: List<Long> = emptyList()) =
            route(Post, "/guilds/$guildID/emojis", GuildEmojiPacket.serializer()) {
                body("name" to name, "image" to encodeBase64(imageData), "roles" to roles)
            }

        fun ModifyGuildEmoji(guildID: Long, id: Long, newName: String, newRoles: List<Long> = emptyList()) =
            route(Patch, "/guilds/$guildID/emojis/$id", GuildEmojiPacket.serializer()) {
                body("name" to newName, "roles" to newRoles)
                ratelimitKey = "/guilds/$guildID/emojis/emojiID"
            }

        fun DeleteGuildEmoji(guildID: Long, id: Long) = route(Delete, "/guilds/$guildID/emojis/$id") {
            ratelimitKey = "/guilds/$guildID/emojis/emojiID"
        }

        fun CreateGuild(packet: CreateGuildPacket) = route(Post, "/guilds") {
            body(CreateGuildPacket.serializer(), packet)
        }

        fun GetGuild(id: Long) = route(Get, "/guilds/$id", GuildCreatePacket.serializer())

        fun ModifyGuild(id: Long, packet: ModifyGuildPacket) =
            route(Patch, "/guilds/$id", GuildCreatePacket.serializer()) {
                body(ModifyGuildPacket.serializer(), packet)
            }

        fun DeleteGuild(id: Long) = route(Delete, "/guild/$id")

        fun GetGuildChannels(id: Long) =
            route(Get, "/guilds/$id/channels", ListSerializer(GuildChannelPacket.polymorphicSerializer))

        fun CreateGuildChannel(guildID: Long, packet: CreateGuildChannelPacket) =
            route(Post, "/guilds/$guildID/channels", GuildChannelPacket.polymorphicSerializer) {
                body(CreateGuildChannelPacket.serializer(), packet)
            }

        fun ModifyGuildChannelPositions(guildID: Long, positions: Map<Long, Int>) =
            route(Patch, "/guilds/$guildID/channels") {
                body(
                    ListSerializer(ModifyPositionPacket.serializer()),
                    positions.map { ModifyPositionPacket(it.key, it.value) })
            }

        fun GetGuildMember(guildID: Long, userID: Long) =
            route(Get, "/guilds/$guildID/members/$userID", GuildMemberPacket.serializer()) {
                ratelimitKey = "/guilds/$guildID/members/userID"
            }

        fun ListGuildMembers(guildID: Long, limit: Int? = null, after: Long? = null) =
            route(Get, "/guilds/$guildID/members", ListSerializer(GuildMemberPacket.serializer())) {
                parameters("limit" to limit, "after" to after)
            }

        fun ModifyGuildMember(guildID: Long, userID: Long, packet: ModifyGuildMemberPacket) =
            route(Patch, "/guilds/$guildID/members/$userID") {
                body(ModifyGuildMemberPacket.serializer(), packet)
            }

        fun ModifyCurrentUserNick(guildID: Long, nickname: String) = route(Patch, "/guilds/$guildID/members/@me/nick") {
            body("nick" to nickname)
        }

        fun RemoveGuildMember(guildID: Long, userID: Long) = route(Delete, "/guilds/$guildID/members/$userID") {
            ratelimitKey = "/guilds/$guildID/members/$userID"
        }

        fun GetGuildBans(guildID: Long) = route(Get, "/guilds/$guildID/bans", ListSerializer(BanPacket.serializer()))

        fun GetGuildBan(guildID: Long, userID: Long) =
            route(Get, "/guilds/$guildID/bans/$userID", BanPacket.serializer()) {
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

        fun GetGuildRoles(guildID: Long) = route(
            Get, "/guilds/$guildID/roles",
            ListSerializer(GuildRolePacket.serializer())
        )

        fun CreateGuildRole(guildID: Long, packet: CreateGuildRolePacket) =
            route(Post, "/guilds/$guildID/roles", GuildRolePacket.serializer()) {
                body(CreateGuildRolePacket.serializer(), packet)
            }

        fun ModifyGuildRole(guildID: Long, roleID: Long, packet: CreateGuildRolePacket) =
            route(Patch, "/guilds/$guildID/roles/$roleID", GuildRolePacket.serializer()) {
                body(CreateGuildRolePacket.serializer(), packet)
            }

        fun DeleteGuildRole(guildID: Long, roleID: Long) = route(Delete, "/guilds/$guildID/roles/$roleID") {
            ratelimitKey = "/guilds/$guildID/roles/roleID"
        }

        fun ModifyGuildRolePosition(guildID: Long, positions: Map<Long, Int>) =
            route(Patch, "/guilds/$guildID/roles", ListSerializer(GuildRolePacket.serializer())) {
                body(
                    ListSerializer(ModifyPositionPacket.serializer()),
                    positions.map { ModifyPositionPacket(it.key, it.value) })
            }

        fun AddGuildMemberRole(guildID: Long, userID: Long, roleID: Long) =
            route(Put, "/guilds/$guildID/members/$userID/roles/$roleID")

        fun RemoveGuildMemberRole(guildID: Long, userID: Long, roleID: Long) =
            route(Delete, "/guilds/$guildID/members/$userID/roles/$roleID")

        fun GetGuildPruneCount(guildID: Long, days: Int? = null) =
            route(Get, "/guilds/$guildID/prune", PruneCountPacket.serializer()) {
                parameters("days" to days)
            }

        fun BeginGuildPrune(guildID: Long, days: Int? = null, computePruneCount: Boolean = true) =
            route(Post, "/guilds/$guildID/prune", PruneCountPacket.serializer()) {
                parameters("days" to days, "compute_prune_count" to computePruneCount)
            }

        fun GetGuildIntegrations(guildID: Long) =
            route(Get, "/guilds/$guildID/integrations", ListSerializer(GuildIntegrationPacket.serializer()))

        fun CreateGuildIntegration(guildID: Long, type: String, id: Long) =
            route(Post, "/guilds/$guildID/integrations") {
                body("type" to type, "id" to id)
            }

        fun ModifyGuildIntegration(guildID: Long, integrationID: Long, packet: ModifyGuildIntegrationPacket) =
            route(Patch, "/guilds/$guildID/integrations/$integrationID") {
                body(ModifyGuildIntegrationPacket.serializer(), packet)
            }

        fun DeleteGuildIntegration(guildID: Long, integrationID: Long) =
            route(Delete, "/guilds/$guildID/integrations/$integrationID")

        fun SyncGuildIntegration(guildID: Long, integrationID: Long) =
            route(Post, "/guilds/$guildID/integrations/$integrationID/sync")

        fun GetGuildInvites(guildID: Long) =
            route(Get, "/guilds/$guildID/invites", ListSerializer(InviteMetadataPacket.serializer()))

        fun GetGuildEmbed(guildID: Long) = route(Get, "/guilds/$guildID/embed", GuildEmbedPacket.serializer())

        fun ModifyGuildEmbed(guildID: Long, enable: Boolean? = null, channelID: Long? = null) =
            route(Patch, "/guilds/$guildID/embed", GuildEmbedPacket.serializer()) {
                body("enabled" to enable, "channel_id" to channelID)
            }

        fun GetGuildVanityUrl(guildID: Long) =
            route(Get, "/guilds/$guildID/vanity-url", PartialInvitePacket.serializer())

        fun GetGuildVoiceRegions(guildID: Long) =
            route(Get, "/guilds/$guildID/regions", ListSerializer(VoiceRegionPacket.serializer()))

        fun GetGuildAuditLog(
            guildID: Long,
            userID: Long? = null, eventType: AuditLogEvent? = null, before: Long? = null, limit: Int? = null
        ) = route(Get, "/guilds/$guildID/audit-logs", AuditLogPacket.serializer()) {
            parameters("user_id" to userID, "action_type" to eventType, "before" to before, "limit" to limit)
        }

        fun GetInvite(inviteCode: String, withCounts: Boolean) =
            route(Get, "/invites/$inviteCode", InvitePacket.serializer()) {
                parameters("with_counts" to withCounts)
                ratelimitKey = "/invites/inviteCode"
            }

        fun DeleteInvite(inviteCode: String) = route(Delete, "/invites/$inviteCode", InvitePacket.serializer()) {
            ratelimitKey = "/invites/inviteCode"
        }

        val GetCurrentUser = route(Get, "/users/@me", UserPacket.serializer())

        fun GetUser(id: Long) = route(Get, "/users/$id", UserPacket.serializer()) {
            ratelimitKey = "/users/userID"
        }

        fun ModifyCurrentUser(username: String? = null, avatarData: AvatarData? = null) =
            route(Patch, "/users/@me", UserPacket.serializer()) {
                body(ModifyCurrentUserPacket.serializer(), ModifyCurrentUserPacket(username, avatarData?.dataUri))
            }

        fun GetCurrentUserGuilds(before: Long? = null, after: Long? = null, limit: Int? = null) =
            route(Get, "/users/@me/guilds", ListSerializer(PartialGuildPacket.serializer())) {
                parameters("before" to before, "after" to after, "limit" to limit)
            }

        fun LeaveGuild(guildID: Long) = route(Delete, "/users/@me/guilds/$guildID")

        fun CreateDM(recipientID: Long) = route(Post, "/users/@me/channels", DmChannelPacket.serializer()) {
            body("recipient_id" to recipientID)
        }

        fun CreateWebhook(channelID: Long, name: String, avatarData: AvatarData? = null) =
            route(Post, "/channels/$channelID/webhooks", WebhookPacket.serializer()) {
                body(CreateWebhookPacket.serializer(), CreateWebhookPacket(name, avatarData?.dataUri))
            }

        fun GetChannelWebhooks(channelID: Long) =
            route(Get, "/channels/$channelID/webhooks", ListSerializer(WebhookPacket.serializer()))

        fun GetGuildWebhooks(guildID: Long) =
            route(Get, "/guilds/$guildID/webhooks", ListSerializer(WebhookPacket.serializer()))

        fun GetWebhook(webhookID: Long) =
            route(Get, "/webhooks/$webhookID", WebhookPacket.serializer())

        fun GetWebhookWithToken(webhookID: Long, token: String) =
            route(Get, "/webhooks/$webhookID/$token", WebhookPacket.serializer()) {
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        fun ModifyWebhook(webhookID: Long, packet: ModifyWebhookPacket) =
            route(Patch, "/webhooks/$webhookID", WebhookPacket.serializer()) {
                body(ModifyWebhookPacket.serializer(), packet)
            }

        fun ModifyWebhookWithToken(webhookID: Long, token: String, packet: ModifyWebhookWithTokenPacket) =
            route(Patch, "/webhooks/$webhookID/$token", WebhookPacket.serializer()) {
                body(ModifyWebhookWithTokenPacket.serializer(), packet)
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        fun DeleteWebhook(webhookID: Long) = route(Delete, "/webhooks/$webhookID")

        fun deleteWebhookWithToken(webhookID: Long, token: String) = route(Delete, "/webhooks/$webhookID/$token") {
            ratelimitKey = "/webhooks/$webhookID/token"
        }

        fun ExecuteWebhook(webhookID: Long, token: String, packet: ExecuteWebhookPacket) =
            route(Post, "/webhooks/$webhookID/$token") {
                parameters("wait" to false)
                body(ExecuteWebhookPacket.serializer(), packet)
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        fun ExecuteWebhookAndWait(webhookID: Long, token: String, packet: ExecuteWebhookPacket) =
            route(Post, "/webhooks/$webhookID/$token", MessageCreatePacket.serializer()) {
                parameters("wait" to true)
                body(ExecuteWebhookPacket.serializer(), packet)
                ratelimitKey = "/webhooks/$webhookID/token"
            }

        val GetGatewayBot = route(Get, "/gateway/bot")

        val ListVoiceRegions = route(Get, "/voice/regions", ListSerializer(VoiceRegionPacket.serializer()))

        val GetApplicationInfo = route(Get, "/oauth2/applications/@me", ApplicationInfoPacket.serializer())
    }
}
