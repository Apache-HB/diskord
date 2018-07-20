package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

internal data class AttachmentPacket(
    val id: Snowflake,
    val filename: String,
    val size: Int,
    val url: String,
    val proxy_url: String,
    val height: Int?,
    val width: Int?
)
