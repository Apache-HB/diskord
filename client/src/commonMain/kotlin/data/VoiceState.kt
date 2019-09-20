package com.serebit.strife.data

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.entities.GuildVoiceChannel
import com.serebit.strife.entities.User
import com.serebit.strife.getUser
import com.serebit.strife.internal.packets.VoiceStatePacket

/**
 * A [User's][User] [VoiceState] is used to represent a their voice connection status in a specific [guild].
 * Discord separates VoiceStates per-guild, and while a [User] is unlikely to have a different state in another guild, we
 * cannot ensure that as they're separated in the API.
 *
 * @property guild The [Guild] this [VoiceState] is associated with.
 * @property context The [BotClient] this [VoiceState] belongs to.
 */
class VoiceState internal constructor(packet: VoiceStatePacket, val guild: Guild, val context: BotClient) {
    /** The ID of the [User]. */
    val userID: Long = packet.user_id
    /** The [GuildVoiceChannel] the user is connected to. `null` if the user is not in any channel. */
    val voiceChannel: GuildVoiceChannel? = packet.channel_id?.run(guild::getVoiceChannel)
    /** The session id for this [VoiceState]. */
    val sessionID: String = packet.session_id
    /** Whether this user is deafened by the server. */
    val deafened: Boolean = packet.deaf
    /** Whether this user is muted by the server. */
    val muted: Boolean = packet.mute
    /** Whether this user is locally deafened. */
    val locallyDeafened: Boolean = packet.self_deaf
    /** Whether this user is locally muted. */
    val locallyMuted: Boolean = packet.self_mute
    /** Whether this user is muted by the current client. */
    val suppressed: Boolean = packet.suppress

    /**
     * Get the [GuildMember] this [VoiceState] belongs to. Returns the [GuildMember], or `null` if we don't have access
     * to the member.
     */
    suspend fun getMember(): GuildMember? = guild.getMember(userID)

    /** Get the [User] this [VoiceState] belongs to. Returns the [User], or `null` if we don't have access to the user. */
    suspend fun getUser(): User? = context.getUser(userID)

    override fun toString() = "VoiceState(sessionID=$sessionID, userID=$userID, guild=${guild.id}" +
            "channel=${voiceChannel?.id}, deafened=$deafened, muted=$muted, locallyDeafened=$locallyDeafened, " +
            "locallyMuted=$locallyMuted, suppressed=$suppressed)"
}

/** Convert this [VoiceStatePacket] to a user-facing [VoiceState]. */
internal fun VoiceStatePacket.toVoiceState(guild: Guild, context: BotClient) = VoiceState(this, guild, context)
