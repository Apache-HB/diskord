package com.serebit.diskord.data

import com.serebit.data.UserData
import com.serebit.data.RoleData

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class MessageData(
    val id: Int,
    val channel_id: Int,
    val guild_id: Int,
    val content: String,
    val time,
    val tts: Boolean,
    val mention_everyone: Boolean,
    val mentions: Array<UserData>,
    val mentions_roles: Array<RoleData>
)