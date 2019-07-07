package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.internal.packets.ActivityPacket
import com.serebit.strife.internal.packets.PresencePacket
import com.soywiz.klock.DateTimeTz
import kotlin.experimental.or

internal class PresenceData(packet: PresencePacket, val guild: GuildData, val context: BotClient) {
    val userID = packet.user.id
    val status = packet.status
    val clientStatus = StatusData(packet.client_status)
    val game = packet.game?.let { ActivityData(it) }
    val activities = packet.activities.map { ActivityData(it) }

    class StatusData(packet: PresencePacket.StatusPacket) {
        val desktop = packet.desktop
        val mobile = packet.mobile
        val web = packet.web
    }
}

internal fun PresencePacket.toData(guild: GuildData, context: BotClient) = PresenceData(this, guild, context)

internal class ActivityData(packet: ActivityPacket) {
    val name = packet.name
    val type = packet.type
    val url = packet.url
    val timestamps = packet.timestamps?.let { TimestampsData(it) }
    val applicationID = packet.application_id
    val details = packet.details
    val state = packet.state
    val party = packet.party?.let { PartyData(it) }
    val assets = packet.assets?.let { AssetsData(it) }
    val secrets = packet.secrets?.let { SecretsData(it) }
    val instance = packet.instance
    val flags = Flags.values().filter { it.value.or(packet.flags) == packet.flags }

    class TimestampsData(packet: ActivityPacket.Timestamps) {
        val start = packet.start?.let { DateTimeTz.fromUnixLocal(it) }
        val end = packet.end?.let { DateTimeTz.fromUnixLocal(it) }
    }

    class PartyData(packet: ActivityPacket.Party) {
        val id = packet.id
        val currentSize = packet.size?.get(0)
        val maxSize = packet.size?.get(1)
    }

    class AssetsData(packet: ActivityPacket.Assets) {
        val largeImage = packet.large_image
        val largeText = packet.large_image
        val smallImage = packet.small_image
        val smallText = packet.small_text
    }

    class SecretsData(packet: ActivityPacket.Secrets) {
        val join = packet.join
        val spectate = packet.spectate
        val match = packet.match
    }

    enum class Flags(val value: Short) {
        INSTANCE(1 shl 0),
        JOIN(1 shl 1),
        SPECTATE(1 shl 2),
        JOIN_REQUEST(1 shl 3),
        SYNC(1 shl 4),
        PLAY(1 shl 5)
    }
}
