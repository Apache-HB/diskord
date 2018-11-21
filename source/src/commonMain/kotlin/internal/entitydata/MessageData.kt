package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket

internal class MessageData(packet: MessageCreatePacket) {
    val id = packet.id
    val channelId = packet.channel_id
    val guildId = packet.guild_id
    val author = packet.author
    val member = packet.member
    var content = packet.content
    var createdAt = packet.timestamp.toDateTime()
    var editedAt = packet.edited_timestamp?.toDateTime()
    val isTextToSpeech = packet.tts
    var mentionsEveryone = packet.mention_everyone
    var mentionedUsers = packet.mentions
    var mentionedRoleIds = packet.mention_roles
    var attachments = packet.attachments
    var embeds = packet.embeds
    var reactions = packet.reactions
    val nonce = packet.nonce
    var isPinned = packet.pinned
    val webhookId = packet.webhook_id
    val type = packet.type
    val activity = packet.activity
    val application = packet.application

    fun update(packet: PartialMessagePacket) = apply {
        packet.content?.let { content = it }
        packet.edited_timestamp?.let { editedAt = it.toDateTime() }
        packet.mention_everyone?.let { mentionsEveryone = it }
        packet.mentions?.let { mentionedUsers = it }
        packet.mention_roles?.let { mentionedRoleIds = it }
        packet.attachments?.let { attachments = it }
        packet.embeds?.let { embeds = it }
        packet.reactions?.let { reactions = it }
        packet.pinned?.let { isPinned = it }
    }
}
