package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.findChannelInCaches
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket

internal class MessageData(packet: MessageCreatePacket, override val context: Context) : EntityData {
    override val id = packet.id
    val channel = context.findChannelInCaches(packet.channel_id)!! as TextChannelData
    val guild = packet.guild_id?.let { context.guildCache[it] }
    val author = context.userCache[packet.author.id]
    val member = packet.member
    var content = packet.content
    var createdAt = packet.timestamp.toDateTime()
    var editedAt = packet.edited_timestamp?.toDateTime()
    val isTextToSpeech = packet.tts
    var mentionsEveryone = packet.mention_everyone
    var mentionedUsers = packet.mentions.mapNotNull { context.userCache[it.id] }
    var mentionedRoles = packet.mention_roles.mapNotNull { guild!!.roles.findById(it) }
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
        packet.mentions?.let { users -> mentionedUsers = users.mapNotNull { context.userCache[it.id] } }
        packet.mention_roles?.let { ids -> mentionedRoles = ids.mapNotNull { guild!!.roles.findById((it)) } }
        packet.attachments?.let { attachments = it }
        packet.embeds?.let { embeds = it }
        packet.reactions?.let { reactions = it }
        packet.pinned?.let { isPinned = it }
    }
}

internal fun MessageCreatePacket.toData(context: Context) = MessageData(this, context)
