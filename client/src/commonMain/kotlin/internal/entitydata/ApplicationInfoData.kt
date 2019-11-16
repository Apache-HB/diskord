package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.entities.ApplicationInfo
import com.serebit.strife.internal.packets.ApplicationInfoPacket

internal class ApplicationInfoData(packet: ApplicationInfoPacket, override val context: BotClient) :
    EntityData<ApplicationInfoPacket, ApplicationInfo> {
    override val id = packet.id
    override val lazyEntity by lazy { ApplicationInfo(this) }

    var name = packet.name
    var description = packet.description
    var public = packet.bot_public
    var requireCodeGrant = packet.bot_require_code_grant
    // We do not want to pull this user into the cache because it’s always missing all optional fields
    var owner = packet.owner.toData(context)

    override fun update(packet: ApplicationInfoPacket) {
        name = packet.name
        description = packet.description
        public = packet.bot_public
        requireCodeGrant = packet.bot_require_code_grant
        owner = packet.owner.toData(context)
    }
}

internal fun ApplicationInfoPacket.toData(context: BotClient) = ApplicationInfoData(this, context)
