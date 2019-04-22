package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.entities.User
import com.serebit.strife.internal.network.Gateway

/**
 * The [ReadyEvent] is dispatched when a client has completed the initial handshake with the [Gateway]. The
 * [ReadyEvent] can be the largest and most complex [Event] the [Gateway] will send, as it contains all the state
 * required for a client to begin interacting with the rest of the platform.
 *
 * @property user The bot-client (self) user
 * @property dmChannels A list of any [DmChannel] the bot has open.
 */
class ReadyEvent internal constructor(
    override val context: Context,
    val user: User,
    val dmChannels: List<DmChannel>
) : Event
