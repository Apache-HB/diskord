package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

data class AttachmentData(
    val id: Snowflake,
    val filename: String,
    val size: Int,
    val url: String,
    val proxy_url: String,
    val height: Int?,
    val width: Int?
)
