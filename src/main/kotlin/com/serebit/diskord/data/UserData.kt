package com.serebit.diskord.data

data class UserData(
    val id: Long,
    val name: String,
    val discriminator: Int,
    val created: String,
    val avatar: String
)