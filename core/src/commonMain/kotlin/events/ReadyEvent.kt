package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.entities.User

class ReadyEvent internal constructor(
    override val context: Context,
    val user: User,
    val dmChannels: List<DmChannel>
) : Event
