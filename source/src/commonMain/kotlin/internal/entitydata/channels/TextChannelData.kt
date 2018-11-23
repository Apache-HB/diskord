package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.data.DateTime
import com.serebit.diskord.internal.entitydata.MessageData

internal interface TextChannelData : ChannelData {
    val lastMessage: MessageData?
    var lastPinTime: DateTime?
    val messages: MutableList<MessageData>
}
