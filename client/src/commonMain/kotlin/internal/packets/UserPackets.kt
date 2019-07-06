package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class UserPacket(
    override val id: Long,
    val username: String,
    val discriminator: Short,
    val avatar: String? = null,
    val bot: Boolean = false,
    val mfa_enabled: Boolean? = null,
    val locale: String? = null,
    val verified: Boolean? = null,
    val email: String? = null,
    val flags: Int? = null,
    val premium_type: Byte? = null
) : EntityPacket

@Serializable
internal data class BasicUserPacket(override val id: Long) : EntityPacket
