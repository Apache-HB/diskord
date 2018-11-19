package com.serebit.diskord.entities

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.BanGuildMember
import com.serebit.diskord.internal.network.endpoints.GetGuild
import com.serebit.diskord.internal.network.endpoints.KickGuildMember

/**
 * Represents a Discord guild (aka "server"), or a self-contained community of users. Guilds contain their own text
 * and voice channels, and can be customized further with [roles][Role] to segment members into different subgroups.
 */
class Guild internal constructor(override val id: Long) : Entity {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetGuild(id))
            ?: throw EntityNotFoundException("Invalid guild instantiated with ID $id.")
    /**
     * The name of a Guild is not unique across Discord, and as such, any two guilds can have the same name. Guild
     * names are subject to similar restrictions as those of usernames, and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    val name get() = packet.name
    val joinedAt get() = packet.joinedAt
    val channels get() = packet.allChannels
    val textChannels get() = packet.textChannels
    val voiceChannels get() = packet.voiceChannels
    val channelCategories get() = packet.channelCategories
    val afkChannel get() = packet.afkChannel
    val systemChannel get() = packet.systemChannel
    val widgetChannel get() = packet.widgetChannel
    val afkTimeout get() = packet.afk_timeout
    val members get() = packet.memberObjects
    val roles get() = packet.roleObjects
    val owner get() = packet.ownerMember
    val permissions get() = packet.permissionsList
    val defaultMessageNotifications get() = packet.default_message_notifications
    val explicitContentFilter get() = packet.explicit_content_filter
    val enabledFeatures get() = packet.features
    val verificationLevel get() = packet.verification_level
    val mfaLevel get() = packet.mfa_level
    val isEmbedEnabled get() = packet.embed_enabled
    val embedChannel get() = packet.embedChannel
    val icon: String? get() = packet.icon
    val splashImage: String? get() = packet.splash
    val region: String get() = packet.region
    val isLarge: Boolean get() = packet.large

    fun kick(user: User): Boolean = Requester.sendRequest(KickGuildMember(id, user.id))

    fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        Requester.sendRequest(
            BanGuildMember(id, user.id), mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        )

    companion object {
        const val NAME_MIN_LENGTH = 2
        const val NAME_MAX_LENGTH = 32
        val NAME_LENGTH_RANGE = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}
