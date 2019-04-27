package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.data.Activity
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.internal.entitydata.UserStatus

/**
 * A user's presence is their current state on a guild. This event is sent when a user's presence or info,
 * such as name or avatar, is updated.
 */
class PresenceUpdateEvent(
    override val context: Context,
    val guild: Guild,
    val member: GuildMember,
    val activity: Activity? = null,
    val status: UserStatus
) : Event
