package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse

internal class MessageData(packet: MessageCreatePacket, override val context: Context) : EntityData {
    override val id = packet.id
    val channel = context.getTextChannelData(packet.channel_id)!!
    val guild = packet.guild_id?.let { context.guildCache[it] }
    val author = context.userCache[packet.author.id]
    val member = packet.member
    var content = packet.content
    var createdAt = DateFormat.ISO_FORMAT.parse(packet.timestamp)
    var editedAt = packet.edited_timestamp?.let { DateFormat.ISO_FORMAT.parse(it) }
    val isTextToSpeech = packet.tts
    var mentionsEveryone = packet.mention_everyone
    var mentionedUsers = packet.mentions.mapNotNull { context.userCache[it.id] }
    var mentionedRoles = packet.mention_roles.mapNotNull { guild!!.roles[it] }
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
        packet.edited_timestamp?.let { editedAt = DateFormat.ISO_FORMAT.parse(it) }
        packet.mention_everyone?.let { mentionsEveryone = it }
        packet.mentions?.let { us ->
            mentionedUsers = us.mapNotNull { context.userCache[it.id] }
        }
        packet.mention_roles?.let { ids ->
            mentionedRoles = ids.mapNotNull { guild!!.roles[it] }
        }
        packet.attachments?.let { attachments = it }
        packet.embeds?.let { embeds = it }
        packet.reactions?.let { reactions = it }
        packet.pinned?.let { isPinned = it }
    }
}

internal fun MessageCreatePacket.toData(context: Context) = MessageData(this, context)
