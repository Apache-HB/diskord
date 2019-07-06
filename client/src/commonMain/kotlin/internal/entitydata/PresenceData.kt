package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.internal.packets.PresencePacket

internal class PresenceData(packet: PresencePacket, val guild: GuildData, val context: BotClient) {


    class StatusData(packet: PresencePacket.StatusPacket) {
        val desktop = packet.desktop
        val mobile = packet.mobile
        val web = packet.web
    }
}