package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Avatar
import com.serebit.strife.data.AvatarData
import com.serebit.strife.data.Permission
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMessageChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.ExecuteWebhookPacket
import com.serebit.strife.internal.packets.ModifyWebhookPacket
import com.serebit.strife.internal.packets.WebhookPacket
import io.ktor.http.*

/**
 * A [Webhook] is an entity that can be used to send messages to a [TextChannel] without consuming the bot's ratelimit.
 * The [author][Message.getAuthor] will be different from this bot, and can have custom name and [Avatar].
 */
class Webhook internal constructor(
    override val context: BotClient,
    private val guildData: GuildData,
    private val channelData: GuildMessageChannelData<*, *>,
    private val packet: WebhookPacket
) : Entity {
    /** The ID of this [Webhook]. */
    override val id: Long = packet.id

    /** A secret token that can be used to interact with this [Webhook] without having access to a [BotClient]. */
    suspend fun getToken(): String = packet.token

    /** The name of this [Webhook]. */
    suspend fun getName(): String? = packet.name

    /** The name of this [Webhook]. */
    suspend fun getAvatar(): Avatar? = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default.BLURPLE

    /** The [Guild] in which this [Webhook] exists. */
    suspend fun getGuild(): Guild = guildData.lazyEntity

    /**  The [GuildTextChannel] this [Webhook] sends messages to.*/
    suspend fun getChannel(): GuildMessageChannel = channelData.lazyEntity

    private val userData = context.cache.pullUserData(packet.user!!)

    /** The [User] who created this [Webhook]. */
    suspend fun getUser(): User = userData.lazyEntity

    /**
     * Sends a [Message] to this webhook's channel. Either [text] or [embeds] has to be provided, or both.
     * Additionally, the webhook's default name and avatar can be overridden by providing [authorName] and
     * [authorAvatar] parameters, respectively. If this is intended to be a Text-to-Speech message, [tts] can be set to
     * `true`.
     *
     * Returns the created message, or `null` on failure.
     */
    suspend fun send(
        text: String? = null,
        embeds: List<EmbedBuilder>? = null,
        authorName: String? = null,
        authorAvatar: String? = null,
        tts: Boolean? = null
    ): Message? {
        text?.apply {
            require(length in 1..Message.MAX_LENGTH) {
                "Message length must be between 1 - ${Message.MAX_LENGTH}"
            }
        }

        embeds?.apply {
            require(size <= 10) { "You cannot send more than 10 embeds" }
        }

        text
            ?: embeds?.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Either text or embed has to be provided")

        return context.requester.sendRequest(
            Route.ExecuteWebhookAndWait(
                id, getToken(), ExecuteWebhookPacket(text, authorName, authorAvatar, tts, embeds = embeds?.map {
                    it.build()
                })
            )
        ).value?.toData(channelData, context)?.lazyEntity
    }

    /**
     * Modify this [Webhook]'s [name], [avatar] or [channelID]. **Requires [Permission.ManageWebhooks].** Returns the
     * modified [Webhook], or null on failure.
     */
    suspend fun modify(
        name: String? = null,
        avatar: AvatarData? = null,
        channelID: Long? = null
    ): Webhook? =
        context.requester.sendRequest(Route.ModifyWebhook(id, ModifyWebhookPacket(name, avatar?.dataUri, channelID)))
            .value
            ?.toEntity(context, guildData, channelData)

    /**
     * Delete this [Webhook]. **Must be the user who created this webhook or have [Permission.ManageWebhooks].**
     * Returns `true` on success, or `false` on failure.
     */
    suspend fun delete(): Boolean = context.requester.sendRequest(Route.DeleteWebhook(id)).status.isSuccess()
}

/**
 * Modify this [Webhook]'s [name], [avatar] or [channel]. **Requires [Permission.ManageWebhooks].** Returns the
 * modified [Webhook], or null on failure.
 */
suspend fun Webhook.modify(
    name: String? = null,
    avatar: AvatarData? = null,
    channel: GuildMessageChannel? = null
): Webhook? = modify(name, avatar, channel?.id)

internal fun WebhookPacket.toEntity(
    context: BotClient, guildData: GuildData, channelData: GuildMessageChannelData<*, *>
) = Webhook(context, guildData, channelData, this)
