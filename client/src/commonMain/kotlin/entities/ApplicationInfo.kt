package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.getUser
import com.serebit.strife.internal.packets.ApplicationInfoPacket

/**
 * [ApplicationInfo] describes a discord application (a game, a bot, or any other app).
 * Right now, it is only possible to query this information for the current bot user.
 */
class ApplicationInfo internal constructor(packet: ApplicationInfoPacket, val context: BotClient) {
    val id = packet.id

    val name = packet.name
    val description = packet.description
    val isPublic = packet.bot_public
    val requiresCodeGrant = packet.bot_require_code_grant

    private val ownerID = packet.owner.id

    suspend fun getOwner() = context.getUser(ownerID)
}
