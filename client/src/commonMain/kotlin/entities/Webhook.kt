package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Avatar
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMessageChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.ExecuteWebhookPacket
import com.serebit.strife.internal.packets.WebhookPacket

/**
 * A [Webhook] is an entity that can be used to send messages to a [TextChannel] without consuming the bot's ratelimit.
 * The [Message.author] will be different from this bot, and can have custom name and [Avatar].
 */
class Webhook internal constructor(
    override val context: BotClient,
    private val guildData: GuildData,
    private val channelData: GuildMessageChannelData<*, *>,
    packet: WebhookPacket
) : Entity {
    /** The ID of this [Webhook]. */
    override val id: Long = packet.id
    /** A secret token that can be used to interact with this [Webhook] without having access to a [BotClient]. */
    val token: String = packet.token
    /** The name of this [Webhook]. */
    val name: String? = packet.name
    /** The name of this [Webhook]. */
    val avatar: Avatar? = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default.BLURPLE
    /** The [Guild] in which this [Webhook] exists. */
    val guild: Guild get() = guildData.lazyEntity
    /**  The [GuildTextChannel] this [Webhook] sends messages to.*/
    val channel: GuildMessageChannel get() = channelData.lazyEntity
    private val userData = context.cache.pullUserData(packet.user!!)
    /** The [User] who created this [Webhook]. */
    val user: User get() = userData.lazyEntity

    /**
     * Sends a [Message] to this [Webhook]'s [channel]. Either [text] or [embeds] has to be provided, or both.
     * Additionally, the [Webhook]'s default [name] and [avatar] can be overridden by providing [authorName] and
     * [authorAvatar] parameters, respectively. If this is intended to be a Text-to-Speech message, [tts] can be set to
     * `true`.
     *
     * Returns the created [Message], or `null` on failure.
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

        val packet = ExecuteWebhookPacket(text, authorName, authorAvatar, tts, embeds = embeds?.map { it.build() })

        return context.requester.sendRequest(Route.ExecuteWebhookAndWait(id, token, packet))
            .value
            ?.toData(channelData, context)
            ?.lazyEntity
    }
}

internal fun WebhookPacket.toEntity(
    context: BotClient, guildData: GuildData, channelData: GuildMessageChannelData<*, *>
) = Webhook(context, guildData, channelData, this)