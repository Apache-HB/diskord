package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.getUser
import com.serebit.strife.internal.packets.ApplicationInfoPacket

/**
 * [ApplicationInfo] describes a discord application (a game, a bot, or any other app).
 * Right now, it is only possible to query this information for the current bot user.
 *
 * @property context The BotClient this [ApplicationInfo] resides in.
 */
class ApplicationInfo internal constructor(packet: ApplicationInfoPacket, val context: BotClient) {
    /** The ID of the app in the Discord Dev. portal */
    val id = packet.id

    /** The name of the app in the Discord Dev. portal */
    val name = packet.name

    /** The description of the app in the Discord Dev. portal */
    val description = packet.description

    /** When false only app owner can join the app's bot to guilds  */
    val isPublic = packet.bot_public

    /** when true the app's bot will only join upon completion of the full oauth2 code grant flow */
    val requiresCodeGrant = packet.bot_require_code_grant

    private val ownerID = packet.owner.id

    /** Returns the [User] which owns this app */
    suspend fun getOwner() = context.getUser(ownerID)
}
