package com.serebit.diskord.events

import com.serebit.diskord.Context

interface Event {
    val context: Context
}
