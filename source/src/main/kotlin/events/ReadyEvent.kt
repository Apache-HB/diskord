package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.User

data class ReadyEvent internal constructor(override val context: Context, val user: User) : Event
