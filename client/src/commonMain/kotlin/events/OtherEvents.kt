package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.entities.User

/**
 * The [ReadyEvent] is dispatched when a client has completed the initial handshake with Discord's API. This event can
 * be the largest and most complex [Event] to be sent by Discord, as it contains all the state required for a client
 * to begin interacting with the rest of the platform.
 *
 * @property user The bot-client (self) user
 * @property dmChannels A list of any [DmChannel] the bot has open.
 */
class ReadyEvent internal constructor(
    override val context: BotClient,
    val user: User,
    val dmChannels: List<DmChannel>
) : Event

/**
 * The [ResumeEvent] is dispatched when a client has resumed an existing Discord session to the gateway, after
 * being disconnected for one of a number of reasons.
 */
class ResumeEvent internal constructor(override val context: BotClient) : Event

/**
 * Received when information about a [User] was updated.
 *
 * @property user The updated User.
 */
class UserUpdateEvent internal constructor(override val context: BotClient, val user: User) : Event
