package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.data.Activity
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.internal.entitydata.UserStatus

/**
 * A user's presence is their current state on a guild. This event is sent
 * when a user's presence or info, such as name or avatar, is updated.
 *
 * @property guild The [Guild] in which the update took place.
 * @property member The [GuildMember] whose information was updated.
 * @property activity The [Activity] of the update.
 * @property status The user's [status][UserStatus].
 */
class PresenceUpdateEvent(
    override val context: BotClient,
    val guild: Guild,
    val member: GuildMember,
    val activity: Activity? = null,
    val status: UserStatus
) : Event
