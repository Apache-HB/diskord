package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.data.DateTime
import com.serebit.diskord.internal.entitydata.EntityData
import com.serebit.diskord.internal.entitydata.MessageData

internal interface ChannelData : EntityData {
    val type: Int
}

internal interface TextChannelData : ChannelData {
    var lastMessageId: Long?
    var lastPinTime: DateTime?
    val messages: MutableList<MessageData>
}
