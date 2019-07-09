package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.data.Presence
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.entities.User

/**
 * Sent when a [User]'s [Presence] or info, such as name or avatar, is updated.
 *
 * @property guild The [Guild] in which the update took place.
 * @property member The [GuildMember] whose information was updated.
 * @property presence The [User]'s new [Presence].
 */
class PresenceUpdateEvent(
    override val context: BotClient,
    val guild: Guild,
    val member: GuildMember,
    val presence: Presence
) : Event
